package org.example.finance.transaction.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.example.finance.category.model.Category;
import org.example.finance.common.model.BaseEntity;
import org.example.finance.counterparty.model.Counterparty;
import org.example.finance.department.model.Department;
import org.example.finance.user.model.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "financial_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialTransaction extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.DRAFT;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "operation_date", nullable = false)
    private LocalDate operationDate;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private Counterparty counterparty;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_of_id", unique = true)
    private FinancialTransaction reversalOf;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "confirmed_at")
    private Instant confirmedAt;
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntry> entries = new ArrayList<>();

    public FinancialTransaction(TransactionType type, BigDecimal amount, String currency, LocalDate date, Department department, Category category, Counterparty counterparty, User author) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.operationDate = date;
        this.department = department;
        this.category = category;
        this.counterparty = counterparty;
        this.author = author;
    }

    public void changeDetails(BigDecimal amount, LocalDate date, Category category, Counterparty counterparty) {
        requireDraft();
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.amount = amount;
        this.operationDate = date;
        this.category = category;
        this.counterparty = counterparty;
    }

    public void addEntry(TransactionEntry entry) {
        requireDraft();
        entries.add(entry);
    }

    public void replaceEntries(List<TransactionEntry> replacement) {
        requireDraft();
        entries.clear();
        entries.addAll(replacement);
    }

    public void confirm() {
        requireDraft();
        if (entries.isEmpty()) throw new IllegalStateException("Transaction has no entries");
        status = TransactionStatus.CONFIRMED;
        confirmedAt = Instant.now();
    }

    public void markReversed() {
        if (status != TransactionStatus.CONFIRMED)
            throw new IllegalStateException("Only confirmed transaction can be reversed");
        status = TransactionStatus.REVERSED;
    }

    public void linkReversal(FinancialTransaction original) {
        requireDraft();
        this.reversalOf = original;
    }

    private void requireDraft() {
        if (status != TransactionStatus.DRAFT) throw new IllegalStateException("Confirmed transaction is immutable");
    }

    public List<TransactionEntry> getEntries() {
        return List.copyOf(entries);
    }
}
