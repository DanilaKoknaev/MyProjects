package org.example.finance.budget.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.example.finance.category.model.Category;
import org.example.finance.common.model.BaseEntity;
import org.example.finance.department.model.Department;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budgets", uniqueConstraints = @UniqueConstraint(columnNames = {"department_id", "category_id", "period_start", "period_end"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 10)
    private BudgetPeriodType periodType;
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    @Column(name = "planned_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal plannedAmount;
    @Column(nullable = false, length = 3)
    private String currency;

    public Budget(Department department, Category category, BudgetPeriodType periodType, LocalDate start, LocalDate end, BigDecimal amount, String currency) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Planned amount must be positive");
        this.department = department;
        this.category = category;
        this.periodType = periodType;
        this.periodStart = start;
        this.periodEnd = end;
        this.plannedAmount = amount;
        this.currency = currency;
    }

    public void update(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Planned amount must be positive");
        this.plannedAmount = amount;
    }
}
