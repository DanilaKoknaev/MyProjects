package org.example.finance.report.dto;

import org.example.finance.budget.dto.BudgetDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BudgetExecutionReport(UUID departmentId, LocalDate from, LocalDate to, List<BudgetDto> budgets) {
}
