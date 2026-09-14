package org.example.finance.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CashFlowReport(UUID departmentId, String currency, LocalDate from, LocalDate to, BigDecimal totalIncome,
                             BigDecimal totalExpense, BigDecimal netCashFlow, List<CategoryLine> categories) {
    public record CategoryLine(String category, BigDecimal income, BigDecimal expense, BigDecimal net) {
    }
}
