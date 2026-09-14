package org.example.finance;

import org.example.finance.user.model.Role;
import org.example.finance.user.model.User;
import org.example.finance.user.repository.UserRepository;
import org.example.finance.department.model.Department;
import org.example.finance.department.repository.DepartmentRepository;
import org.example.finance.account.model.Account;
import org.example.finance.account.model.AccountType;
import org.example.finance.account.repository.AccountRepository;
import org.example.finance.category.model.Category;
import org.example.finance.category.model.CategoryType;
import org.example.finance.category.repository.CategoryRepository;
import org.example.finance.transaction.repository.TransactionEntryRepository;
import org.example.finance.audit.repository.AuditEventRepository;
import org.example.finance.audit.model.AuditAction;
import org.example.finance.budget.model.Budget;
import org.example.finance.budget.model.BudgetPeriodType;
import org.example.finance.budget.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {
    private static final String TEST_PASSWORD = UUID.randomUUID().toString();
    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("app.security.jwt.secret", () ->
                "integration-test-secret-with-more-than-32-characters");
        r.add("spring.security.oauth2.client.registration.google.client-id", () ->
                "test-client");
        r.add("spring.security.oauth2.client.registration.google.client-secret", () ->
                "test-secret");
    }

    @LocalServerPort
    int port;
    @Autowired
    UserRepository users;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    DepartmentRepository departments;
    @Autowired
    AccountRepository accounts;
    @Autowired
    CategoryRepository categories;
    @Autowired
    TransactionEntryRepository entries;
    @Autowired
    AuditEventRepository audit;
    @Autowired
    BudgetRepository budgets;

    @BeforeEach
    void seed() {
        if (users.findByEmailIgnoreCase("admin@example.com").isEmpty())
            users.save(new User("admin@example.com"
                    , encoder.encode(TEST_PASSWORD), "Admin"
                    , Role.ADMIN, null));
    }

    @Test
    void passwordLoginReturnsAccessAndRefreshTokens() throws Exception {
        var request = HttpRequest.newBuilder(URI
                .create("http://localhost:" + port + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString("{\"email\":\"admin@example.com\",\"password\":\""
                                + TEST_PASSWORD + "\"}")).build();
        var response = HttpClient.newHttpClient().send(request
                , HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("accessToken"));
        assertTrue(response.body().contains("refreshToken"));
    }

    @Test
    void googleLoginEndpointStartsAuthorizationFlow() throws Exception {
        var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER).build();
        var response = client.send(HttpRequest.newBuilder(URI
                .create("http://localhost:" + port + "/oauth2/authorization/google"))
                .GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, response.statusCode());
        assertTrue(response.headers().firstValue("location").orElse("")
                .contains("accounts.google.com"));
    }

    @Test
    void transferIsAtomicAndConfirmedOperationIsImmutable() throws Exception {
        var department = departments.save(new Department("OPS", "Operations"));
        var user = users.findByEmailIgnoreCase("admin@example.com").orElseThrow();
        user.update("Admin", Role.ADMIN, department, true);
        users.save(user);
        var source = accounts.save(new Account("Source", AccountType.BANK_ACCOUNT
                , "RUB", department));
        var destination = accounts.save(new Account("Destination"
                , AccountType.BANK_ACCOUNT, "RUB", department));
        String token = loginToken();
        String body = "{\"type\":\"TRANSFER\",\"amount\":100,\"currency\":" +
                "\"RUB\",\"operationDate\":\"2026-09-10\",\"sourceAccountId\":\""
                + source.getId() + "\",\"destinationAccountId\":\""
                + destination.getId() + "\"}";
        var created = send("POST", "/api/v1/transactions", body, token);
        assertEquals(201, created.statusCode());
        String id = extract(created.body(), "id");
        var confirmed = send("POST", "/api/v1/transactions/" +
                id + "/confirm", "", token);
        assertEquals(200, confirmed.statusCode());
        assertEquals(new BigDecimal("-100.0000"), entries.balance(source.getId()));
        assertEquals(new BigDecimal("100.0000"), entries.balance(destination.getId()));
        var rejected = send("PUT", "/api/v1/transactions/" + id, body, token);
        assertEquals(409, rejected.statusCode());
        var deletion = send("DELETE", "/api/v1/transactions/" + id, "", token);
        assertEquals(409, deletion.statusCode());
        assertEquals(1, audit.countByAction(AuditAction.DELETE_ATTEMPT));
        var reversed = send("POST", "/api/v1/transactions/" +
                id + "/reverse", "", token);
        assertEquals(200, reversed.statusCode());
        assertEquals(0, entries.balance(source.getId()).signum());
        assertEquals(0, entries.balance(destination.getId()).signum());
        assertEquals(409, send("POST", "/api/v1/transactions/" +
                id + "/reverse", "", token).statusCode());
    }

    @Test
    void budgetExecutionCountsOnlyConfirmedExpenses() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        var department = departments.save(new Department("B" + suffix, "Budget"));
        var user = users.findByEmailIgnoreCase("admin@example.com").orElseThrow();
        user.update("Admin", Role.ADMIN, department, true);
        users.save(user);
        var account = accounts.save(new Account("Card", AccountType.CORPORATE_CARD
                , "RUB", department));
        var category = categories.save(new Category("Training "
                + suffix, CategoryType.EXPENSE, null));
        budgets.save(new Budget(department, category, BudgetPeriodType.MONTH
                , java.time.LocalDate.of(2026, 9, 1)
                , java.time.LocalDate.of(2026, 9, 30)
                , new BigDecimal("1000"), "RUB"));
        String token = loginToken();
        String draft = "{\"type\":\"EXPENSE\",\"amount\":300,\"currency\":\"RUB\"," +
                "\"operationDate\":\"2026-09-11\",\"sourceAccountId\":\""
                + account.getId() + "\",\"categoryId\":\""
                + category.getId() + "\"}";
        assertEquals(201, send("POST", "/api/v1/transactions"
                , draft, token).statusCode());
        String confirmedBody = draft.replace("300", "200");
        var created = send("POST", "/api/v1/transactions", confirmedBody, token);
        String id = extract(created.body(), "id");
        assertEquals(200, send("POST", "/api/v1/transactions/"
                + id + "/confirm", "", token).statusCode());
        var report = send("GET", "/api/v1/reports/budget-execution?departmentId="
                + department.getId() + "&from=2026-09-01&to=2026-09-30", "", token);
        assertEquals(200, report.statusCode());
        assertTrue(report.body().contains("\"actualAmount\":200"), report.body());
        assertTrue(report.body().contains("\"executionPercent\":20.00"), report.body());
        var cashFlow = send("GET", "/api/v1/reports/cash-flow?departmentId="
                + department.getId() + "&currency=RUB&from=2026-09-01&to=2026-09-30", ""
                , token);
        assertEquals(200, cashFlow.statusCode());
        assertTrue(cashFlow.body().contains("\"totalExpense\":200"), cashFlow.body());
        assertTrue(cashFlow.body().contains("\"netCashFlow\":-200"), cashFlow.body());
    }

    private String loginToken() throws Exception {
        var r = send("POST", "/api/v1/auth/login"
                , "{\"email\":\"admin@example.com\",\"password\":\""
                        + TEST_PASSWORD + "\"}", null);
        return extract(r.body(), "accessToken");
    }

    private HttpResponse<String> send(String method, String path, String body
            , String token) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        b.method(method, body.isEmpty() ? HttpRequest.BodyPublishers.noBody() : HttpRequest
                .BodyPublishers.ofString(body));
        return HttpClient.newHttpClient().send(b.build()
                , HttpResponse.BodyHandlers.ofString());
    }

    private String extract(String json, String field) {
        var m = Pattern.compile("\\\"" + field + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
                .matcher(json);
        assertTrue(m.find(), json);
        return m.group(1);
    }
}
