package org.example.finance.budget.service;

import org.example.finance.budget.dto.BudgetDto;
import org.example.finance.budget.model.Budget;
import org.example.finance.budget.model.BudgetPeriodType;
import org.example.finance.budget.repository.BudgetRepository;
import org.example.finance.category.model.CategoryType;
import org.example.finance.category.repository.CategoryRepository;
import org.example.finance.common.exception.NotFoundException;
import org.example.finance.department.repository.DepartmentRepository;
import org.example.finance.security.CurrentUserService;
import org.example.finance.transaction.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class BudgetService {
    private final BudgetRepository repository;
    private final DepartmentRepository departments;
    private final CategoryRepository categories;
    private final TransactionRepository transactions;
    private final CurrentUserService current;

    public BudgetService(BudgetRepository r, DepartmentRepository d, CategoryRepository c, TransactionRepository t, CurrentUserService current) {
        repository = r;
        departments = d;
        categories = c;
        transactions = t;
        this.current = current;
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public BudgetDto create(BudgetDto d) {
        var dept = departments.findById(d.departmentId()).orElseThrow(() -> new NotFoundException("Department"));
        var cat = categories.findById(d.categoryId()).orElseThrow(() -> new NotFoundException("Category"));
        if (cat.getType() != CategoryType.EXPENSE)
            throw new IllegalArgumentException("Budget category must be an expense category");
        var end = periodEnd(d.periodStart(), d.periodType());
        return dto(repository.save(new Budget(dept, cat, d.periodType(), d.periodStart(), end, d.plannedAmount(), d.currency())));
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public BudgetDto update(UUID id, BudgetDto d) {
        var b = get(id);
        b.update(d.plannedAmount());
        return dto(b);
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public void delete(UUID id) {
        repository.delete(get(id));
    }

    @Transactional(readOnly = true)
    public Page<BudgetDto> list(Pageable p) {
        var u = current.get();
        return (current.seesAll(u) ? repository.findAll(p) : repository.findByDepartmentId(u.getDepartment().getId(), p)).map(this::dto);
    }

    public Budget get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Budget"));
    }

    public BudgetDto dto(Budget b) {
        var actual = transactions.confirmedExpenses(b.getDepartment().getId(), b.getCategory().getId(), b.getCurrency(), b.getPeriodStart(), b.getPeriodEnd());
        var percent = actual.multiply(BigDecimal.valueOf(100)).divide(b.getPlannedAmount(), 2, RoundingMode.HALF_UP);
        return new BudgetDto(b.getId(), b.getDepartment().getId(), b.getCategory().getId(), b.getPeriodType(), b.getPeriodStart(), b.getPeriodEnd(), b.getPlannedAmount(), b.getCurrency(), actual, percent);
    }

    private LocalDate periodEnd(LocalDate start, BudgetPeriodType type) {
        if (start.getDayOfMonth() != 1)
            throw new IllegalArgumentException("Budget period must start on the first day of a month");
        if (type == BudgetPeriodType.QUARTER && (start.getMonthValue() - 1) % 3 != 0)
            throw new IllegalArgumentException("Quarter must start in January, April, July or October");
        return type == BudgetPeriodType.MONTH ? start.plusMonths(1).minusDays(1) : start.plusMonths(3).minusDays(1);
    }
}
