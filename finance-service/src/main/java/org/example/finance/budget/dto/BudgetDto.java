package org.example.finance.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.example.finance.budget.model.BudgetPeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetDto(UUID id, @NotNull UUID departmentId, @NotNull UUID categoryId,
                        @NotNull BudgetPeriodType periodType, @NotNull LocalDate periodStart, LocalDate periodEnd,
                        @NotNull @DecimalMin("0.0001") BigDecimal plannedAmount,
                        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency, BigDecimal actualAmount,
                        BigDecimal executionPercent) {
}
