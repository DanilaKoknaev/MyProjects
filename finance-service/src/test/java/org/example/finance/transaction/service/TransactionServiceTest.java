package org.example.finance.transaction.service;

import org.example.finance.account.model.Account;
import org.example.finance.account.model.AccountType;
import org.example.finance.account.repository.AccountRepository;
import org.example.finance.audit.model.AuditAction;
import org.example.finance.audit.service.AuditService;
import org.example.finance.category.model.Category;
import org.example.finance.category.model.CategoryType;
import org.example.finance.category.repository.CategoryRepository;
import org.example.finance.counterparty.repository.CounterpartyRepository;
import org.example.finance.department.model.Department;
import org.example.finance.security.CurrentUserService;
import org.example.finance.transaction.dto.TransactionRequest;
import org.example.finance.transaction.model.FinancialTransaction;
import org.example.finance.transaction.model.TransactionEntry;
import org.example.finance.transaction.model.TransactionStatus;
import org.example.finance.transaction.model.TransactionType;
import org.example.finance.transaction.repository.TransactionRepository;
import org.example.finance.user.model.Role;
import org.example.finance.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock TransactionRepository transactions;
    @Mock AccountRepository accounts;
    @Mock CategoryRepository categories;
    @Mock CounterpartyRepository counterparties;
    @Mock CurrentUserService current;
    @Mock AuditService audit;
    @InjectMocks TransactionService service;

    private UUID sourceId;
    private UUID destinationId;
    private UUID incomeCategoryId;
    private UUID expenseCategoryId;
    private Department department;
    private Account source;
    private Account destination;
    private Category incomeCategory;
    private Category expenseCategory;
    private User actor;

    @BeforeEach
    void setUp() {
        sourceId = UUID.randomUUID();
        destinationId = UUID.randomUUID();
        incomeCategoryId = UUID.randomUUID();
        expenseCategoryId = UUID.randomUUID();
        department = new Department("D", "Department");
        source = new Account("Source", AccountType.BANK_ACCOUNT, "RUB", department);
        destination = new Account("Destination", AccountType.CASH, "RUB", department);
        incomeCategory = new Category("Income", CategoryType.INCOME, null);
        expenseCategory = new Category("Expense", CategoryType.EXPENSE, null);
        actor = new User("accountant@example.com"
                , "hash", "Accountant", Role.ACCOUNTANT, department);
        lenient().when(current.get()).thenReturn(actor);
        lenient().when(accounts.findById(sourceId)).thenReturn(Optional.of(source));
        lenient().when(transactions.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsIncomeWithPositiveEntryAndAudit() {
        when(categories.findById(incomeCategoryId)).thenReturn(Optional.of(incomeCategory));
        service.create(request(TransactionType.INCOME, "150"
                , sourceId, null, incomeCategoryId, "RUB"));
        var saved = savedTransaction();
        assertEquals(new BigDecimal("150"), saved.getEntries().getFirst().getAmount());
        verify(audit).record(any(), any(), eq(AuditAction.CREATED), eq("SUCCESS"), anyString());
    }

    @Test
    void createsExpenseWithNegativeEntry() {
        when(categories.findById(expenseCategoryId)).thenReturn(Optional.of(expenseCategory));
        service.create(request(TransactionType.EXPENSE, "75", sourceId
                , null, expenseCategoryId, "RUB"));
        assertEquals(new BigDecimal("-75"), savedTransaction().getEntries().getFirst().getAmount());
    }

    @Test
    void createsBalancedTransferInOneAggregate() {
        when(accounts.findById(destinationId)).thenReturn(Optional.of(destination));
        service.create(request(TransactionType.TRANSFER, "125"
                , sourceId, destinationId, null, "RUB"));
        var saved = savedTransaction();
        assertEquals(2, saved.getEntries().size());
        assertEquals(0, saved.getEntries().stream().map(TransactionEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).signum());
    }

    @Test
    void rejectsTransferToSameAccount() {
        assertThrows(IllegalArgumentException.class, () -> service.create(
                request(TransactionType.TRANSFER, "10", sourceId
                        , sourceId, null, "RUB")));
        verify(transactions, never()).save(any());
    }

    @Test
    void rejectsTransferBetweenDepartments() {
        var otherDepartment = mock(Department.class);
        when(otherDepartment.getId()).thenReturn(UUID.randomUUID());
        var foreign = new Account("Foreign", AccountType.CASH, "RUB"
                , otherDepartment);
        when(accounts.findById(destinationId)).thenReturn(Optional.of(foreign));
        assertThrows(IllegalArgumentException.class, () -> service.create(
                request(TransactionType.TRANSFER, "10", sourceId, destinationId
                        , null, "RUB")));
    }

    @Test
    void rejectsAccountCurrencyMismatch() {
        assertThrows(IllegalArgumentException.class, () -> service.create(
                request(TransactionType.INCOME, "10", sourceId
                        , null, incomeCategoryId, "USD")));
    }

    @Test
    void rejectsCategoryOfWrongType() {
        when(categories.findById(expenseCategoryId)).thenReturn(Optional.of(expenseCategory));
        assertThrows(IllegalArgumentException.class, () -> service.create(
                request(TransactionType.INCOME, "10", sourceId, null
                        , expenseCategoryId, "RUB")));
    }

    @Test
    void rejectsManualReversalCreation() {
        assertThrows(IllegalArgumentException.class, () -> service.create(
                request(TransactionType.REVERSAL, "10", sourceId
                        , null, null, "RUB")));
    }

    @Test
    void rejectsChangingTypeDuringDraftUpdate() {
        var id = UUID.randomUUID();
        when(transactions.findById(id))
                .thenReturn(Optional.of(transaction(TransactionType.INCOME, incomeCategory)));
        var error = assertThrows(IllegalStateException.class, () -> service.update(id,
                request(TransactionType.EXPENSE, "20", sourceId, null
                        , expenseCategoryId, "RUB")));
        assertEquals("Transaction type cannot be changed", error.getMessage());
    }

    @Test
    void rejectsChangingCurrencyDuringDraftUpdate() {
        var id = UUID.randomUUID();
        when(transactions.findById(id))
                .thenReturn(Optional.of(transaction(TransactionType.INCOME, incomeCategory)));
        assertThrows(IllegalStateException.class, () -> service.update(id,
                request(TransactionType.INCOME, "20", sourceId
                        , null, incomeCategoryId, "USD")));
    }

    @Test
    void rejectsEditingConfirmedTransaction() {
        var id = UUID.randomUUID();
        when(transactions.findById(id)).thenReturn(Optional.of(confirmedTransaction()));
        when(categories.findById(incomeCategoryId)).thenReturn(Optional.of(incomeCategory));
        assertThrows(IllegalStateException.class, () -> service.update(id,
                request(TransactionType.INCOME, "200", sourceId
                        , null, incomeCategoryId, "RUB")));
    }

    @Test
    void confirmsDraftAndWritesAudit() {
        var id = UUID.randomUUID();
        var draft = transaction(TransactionType.INCOME, incomeCategory);
        draft.addEntry(new TransactionEntry(draft, source, new BigDecimal("100")));
        when(transactions.findById(id)).thenReturn(Optional.of(draft));
        assertEquals(TransactionStatus.CONFIRMED, service.confirm(id).status());
        verify(audit).record(any(), eq(id), eq(AuditAction.CONFIRMED)
                , eq("SUCCESS"), anyString());
    }

    @Test
    void reversesConfirmedTransactionWithOppositeEntry() {
        var id = UUID.randomUUID();
        var original = confirmedTransaction();
        when(transactions.findById(id)).thenReturn(Optional.of(original));
        var result = service.reverse(id);
        assertEquals(TransactionType.REVERSAL, result.type());
        assertEquals(new BigDecimal("-100"), result.entries().getFirst().amount());
        assertEquals(TransactionStatus.REVERSED, original.getStatus());
        verify(audit).record(any(), eq(id), eq(AuditAction.REVERSED), eq("SUCCESS"), anyString());
    }

    @Test
    void rejectsReversingDraft() {
        var id = UUID.randomUUID();
        when(transactions.findById(id))
                .thenReturn(Optional.of(transaction(TransactionType.INCOME, incomeCategory)));
        assertThrows(IllegalStateException.class, () -> service.reverse(id));
    }

    @Test
    void deletesDraftAndAuditsAttempt() {
        var id = UUID.randomUUID();
        var draft = transaction(TransactionType.INCOME, incomeCategory);
        when(transactions.findById(id)).thenReturn(Optional.of(draft));
        service.delete(id);
        verify(audit).recordIndependent(any(), eq(id), eq(AuditAction.DELETE_ATTEMPT)
                , eq("SUCCESS"), anyString());
        verify(transactions).delete(draft);
    }

    @Test
    void rejectsDeletingConfirmedTransactionAndAuditsRejection() {
        var id = UUID.randomUUID();
        when(transactions.findById(id)).thenReturn(Optional.of(confirmedTransaction()));
        assertThrows(IllegalStateException.class, () -> service.delete(id));
        verify(audit).recordIndependent(any(), eq(id), eq(AuditAction.DELETE_ATTEMPT)
                , eq("REJECTED"), anyString());
        verify(transactions, never()).delete(any(FinancialTransaction.class));
    }

    @Test
    void rejectsDeletionForAuditorAndAuditsAttempt() {
        actor = new User("auditor@example.com", null
                , "Auditor", Role.AUDITOR, department);
        when(current.get()).thenReturn(actor);
        var id = UUID.randomUUID();
        when(transactions.findById(id))
                .thenReturn(Optional.of(transaction(TransactionType.INCOME, incomeCategory)));
        assertThrows(AccessDeniedException.class, () -> service.delete(id));
        verify(audit).recordIndependent(any(), eq(id), eq(AuditAction.DELETE_ATTEMPT)
                , eq("REJECTED"), anyString());
    }

    private TransactionRequest request(TransactionType type, String amount, UUID sourceAccountId,
                                       UUID destinationAccountId, UUID categoryId, String currency) {
        return new TransactionRequest(type, new BigDecimal(amount), currency, LocalDate.now(),
                sourceAccountId, destinationAccountId, categoryId, null);
    }

    private FinancialTransaction transaction(TransactionType type, Category category) {
        return new FinancialTransaction(type, new BigDecimal("100"), "RUB", LocalDate.now(),
                department, category, null, actor);
    }

    private FinancialTransaction confirmedTransaction() {
        var transaction = transaction(TransactionType.INCOME, incomeCategory);
        transaction.addEntry(new TransactionEntry(transaction, source, new BigDecimal("100")));
        transaction.confirm();
        return transaction;
    }

    private FinancialTransaction savedTransaction() {
        var captor = ArgumentCaptor.forClass(FinancialTransaction.class);
        verify(transactions).save(captor.capture());
        return captor.getValue();
    }
}
