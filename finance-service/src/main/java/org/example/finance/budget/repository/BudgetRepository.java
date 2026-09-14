package org.example.finance.budget.repository;

import org.example.finance.budget.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Page<Budget> findByDepartmentId(UUID departmentId, Pageable pageable);

    List<Budget> findByDepartmentIdAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(UUID departmentId, LocalDate end, LocalDate start);
}
