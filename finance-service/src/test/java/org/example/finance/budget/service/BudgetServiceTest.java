package org.example.finance.budget.service;

import org.example.finance.budget.model.Budget;
import org.example.finance.budget.model.BudgetPeriodType;
import org.example.finance.budget.repository.BudgetRepository;
import org.example.finance.category.model.Category;
import org.example.finance.category.model.CategoryType;
import org.example.finance.category.repository.CategoryRepository;
import org.example.finance.department.model.Department;
import org.example.finance.department.repository.DepartmentRepository;
import org.example.finance.security.CurrentUserService;
import org.example.finance.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BudgetServiceTest {
    @Test
    void executionUsesConfirmedExpenseAmount() {
        var transactions = mock(TransactionRepository.class);
        when(transactions.confirmedExpenses(any(), any(), anyString(), any(), any()))
                .thenReturn(new BigDecimal("250"));
        var service = new BudgetService(mock(BudgetRepository.class)
                , mock(DepartmentRepository.class)
                , mock(CategoryRepository.class), transactions
                , mock(CurrentUserService.class));
        var budget = new Budget(new Department("IT", "IT")
                , new Category("Training", CategoryType.EXPENSE, null)
                , BudgetPeriodType.MONTH, LocalDate.of(2026, 9, 1)
                , LocalDate.of(2026, 9, 30)
                , new BigDecimal("1000"), "RUB");
        var result = service.dto(budget);
        assertEquals(new BigDecimal("250"), result.actualAmount());
        assertEquals(new BigDecimal("25.00"), result.executionPercent());
    }
}
