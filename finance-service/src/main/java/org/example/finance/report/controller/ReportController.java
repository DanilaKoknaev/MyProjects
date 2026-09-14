package org.example.finance.report.controller;

import org.example.finance.report.dto.BudgetExecutionReport;
import org.example.finance.report.dto.CashFlowReport;
import org.example.finance.report.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/cash-flow")
    public CashFlowReport cashFlow(@RequestParam UUID departmentId, @RequestParam @Pattern(regexp = "[A-Z]{3}") String currency, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.cashFlow(departmentId, currency, from, to);
    }

    @GetMapping("/budget-execution")
    public BudgetExecutionReport budget(@RequestParam UUID departmentId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.budgetExecution(departmentId, from, to);
    }
}
