package org.example.finance.transaction.model;

import org.example.finance.account.model.Account;
import org.example.finance.account.model.AccountType;
import org.example.finance.category.model.Category;
import org.example.finance.category.model.CategoryType;
import org.example.finance.department.model.Department;
import org.example.finance.user.model.Role;
import org.example.finance.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialTransactionTest {
    private Department department;
    private Account source;
    private Account destination;
    private Category category;
    private User author;

    @BeforeEach
    void setUp() {
        department = new Department("IT", "Information Technology");
        source = new Account("Source", AccountType.BANK_ACCOUNT, "RUB", department);
        destination = new Account("Destination", AccountType.BANK_ACCOUNT
                , "RUB", department);
        category = new Category("Sales", CategoryType.INCOME, null);
        author = new User("author@example.com", "hash", "Author"
                , Role.ACCOUNTANT, department);
    }

    @Test
    void newTransactionIsDraft() {
        var transaction = income("100");
        assertEquals(TransactionStatus.DRAFT, transaction.getStatus());
        assertNull(transaction.getConfirmedAt());
    }

    @Test
    void amountMustBePositive() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> income("0")),
                () -> assertThrows(IllegalArgumentException.class, () -> income("-1")));
    }

    @Test
    void transactionWithoutEntriesCannotBeConfirmed() {
        var transaction = income("100");
        assertThrows(IllegalStateException.class, transaction::confirm);
        assertEquals(TransactionStatus.DRAFT, transaction.getStatus());
    }

    @Test
    void confirmationChangesStatusAndSetsTimestamp() {
        var transaction = income("100");
        transaction.addEntry(new TransactionEntry(transaction, source, new BigDecimal("100")));
        transaction.confirm();
        assertEquals(TransactionStatus.CONFIRMED, transaction.getStatus());
        assertNotNull(transaction.getConfirmedAt());
    }

    @Test
    void confirmedTransactionIsImmutableAndCannotBeConfirmedAgain() {
        var transaction = confirmedIncome();
        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> transaction.changeDetails(
                        new BigDecimal("200"), LocalDate.now(), category, null)),
                () -> assertThrows(IllegalStateException.class, () -> transaction
                        .replaceEntries(List.of())),
                () -> assertThrows(IllegalStateException.class, () -> transaction
                        .addEntry(
                        new TransactionEntry(transaction, source, BigDecimal.ONE))),
                () -> assertThrows(IllegalStateException.class, transaction::confirm));
    }

    @Test
    void onlyConfirmedTransactionCanBeMarkedReversed() {
        var draft = income("100");
        assertThrows(IllegalStateException.class, draft::markReversed);
        var confirmed = confirmedIncome();
        confirmed.markReversed();
        assertEquals(TransactionStatus.REVERSED, confirmed.getStatus());
        assertThrows(IllegalStateException.class, confirmed::markReversed);
    }

    @Test
    void transferCanContainBalancedEntryPair() {
        var transaction = new FinancialTransaction(TransactionType.TRANSFER, new BigDecimal("100"),
                "RUB", LocalDate.now(), department, null, null, author);
        transaction.addEntry(new TransactionEntry(transaction, source, new BigDecimal("-100")));
        transaction.addEntry(new TransactionEntry(transaction, destination, new BigDecimal("100")));
        transaction.confirm();
        assertEquals(2, transaction.getEntries().size());
        assertEquals(0, transaction.getEntries().stream().map(TransactionEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).signum());
    }

    @Test
    void entriesCannotBeModifiedFromOutsideAggregate() {
        var transaction = income("100");
        transaction.addEntry(new TransactionEntry(transaction, source, new BigDecimal("100")));
        assertThrows(UnsupportedOperationException.class, () -> transaction.getEntries().clear());
        assertEquals(1, transaction.getEntries().size());
    }

    private FinancialTransaction income(String amount) {
        return new FinancialTransaction(TransactionType.INCOME, new BigDecimal(amount), "RUB",
                LocalDate.now(), department, category, null, author);
    }

    private FinancialTransaction confirmedIncome() {
        var transaction = income("100");
        transaction.addEntry(new TransactionEntry(transaction, source, new BigDecimal("100")));
        transaction.confirm();
        return transaction;
    }
}
