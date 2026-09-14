package org.example.finance.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.finance.common.model.BaseEntity;
import org.example.finance.department.model.Department;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Currency;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {
    @Column(nullable = false, length = 200)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountType type;
    @Column(nullable = false, length = 3)
    private String currency;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    @Column(nullable = false)
    private boolean active = true;

    public Account(String name, AccountType type, String currency, Department department) {
        this.name = name;
        this.type = type;
        this.currency = Currency.getInstance(currency).getCurrencyCode();
        this.department = department;
    }

    public void update(String name, AccountType type, boolean active) {
        this.name = name;
        this.type = type;
        this.active = active;
    }
}
