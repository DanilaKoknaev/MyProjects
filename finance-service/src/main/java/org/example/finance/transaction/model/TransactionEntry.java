package org.example.finance.transaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.finance.account.model.Account;
import org.example.finance.common.model.BaseEntity;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionEntry extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private FinancialTransaction transaction;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    public TransactionEntry(FinancialTransaction transaction, Account account, BigDecimal amount) {
        this.transaction = transaction;
        this.account = account;
        this.amount = amount;
    }
}
