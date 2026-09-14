package org.example.finance.report.service;

import org.example.finance.budget.repository.BudgetRepository;
import org.example.finance.budget.service.BudgetService;
import org.example.finance.report.dto.BudgetExecutionReport;
import org.example.finance.report.dto.CashFlowReport;
import org.example.finance.security.CurrentUserService;
import org.example.finance.transaction.model.TransactionType;
import org.example.finance.transaction.repository.TransactionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReportService {
    private final TransactionRepository transactions;
    private final BudgetRepository budgets;
    private final BudgetService budgetService;
    private final CurrentUserService current;

    public ReportService(TransactionRepository t, BudgetRepository b, BudgetService bs, CurrentUserService c) {
        transactions = t;
        budgets = b;
        budgetService = bs;
        current = c;
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER','AUDITOR')")
    public CashFlowReport cashFlow(UUID departmentId, String currency, LocalDate from, LocalDate to) {
        validate(departmentId, from, to);
        var normalized = Currency.getInstance(currency).getCurrencyCode();
        var grouped = new LinkedHashMap<String, BigDecimal[]>();
        for (var row : transactions.cashFlow(departmentId, normalized, from, to)) {
            var values = grouped.computeIfAbsent(row.getCategory(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            values[row.getType() == TransactionType.INCOME ? 0 : 1] = row.getAmount();
        }
        var lines = grouped.entrySet().stream().map(e -> new CashFlowReport.CategoryLine(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[0].subtract(e.getValue()[1]))).toList();
        var income = lines.stream().map(CashFlowReport.CategoryLine::income).reduce(BigDecimal.ZERO, BigDecimal::add);
        var expense = lines.stream().map(CashFlowReport.CategoryLine::expense).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CashFlowReport(departmentId, normalized, from, to, income, expense, income.subtract(expense), lines);
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER','AUDITOR','ACCOUNTANT')")
    public BudgetExecutionReport budgetExecution(UUID departmentId, LocalDate from, LocalDate to) {
        validate(departmentId, from, to);
        current.requireDepartment(current.get(), departmentId);
        return new BudgetExecutionReport(departmentId, from, to, budgets.findByDepartmentIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(departmentId, to, from).stream().map(budgetService::dto).toList());
    }

    private void validate(UUID departmentId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) throw new IllegalArgumentException("Start date must not be after end date");
    }
}
