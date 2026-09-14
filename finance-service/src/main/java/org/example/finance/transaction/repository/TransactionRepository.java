package org.example.finance.transaction.repository;

import org.example.finance.transaction.model.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionRepository extends JpaRepository<FinancialTransaction, UUID>, JpaSpecificationExecutor<FinancialTransaction> {
    Page<FinancialTransaction> findByDepartmentId(UUID departmentId, Pageable pageable);

    @Query("select coalesce(sum(t.amount),0) from FinancialTransaction t where t.department.id=:departmentId and t.category.id=:categoryId and t.currency=:currency and t.type='EXPENSE' and t.status='CONFIRMED' and t.operationDate between :start and :end")
    BigDecimal confirmedExpenses(UUID departmentId, UUID categoryId, String currency, LocalDate start, LocalDate end);

    @Query("select t.category.name as category,t.type as type,sum(t.amount) as amount from FinancialTransaction t where t.department.id=:departmentId and t.currency=:currency and t.type in ('INCOME','EXPENSE') and t.status='CONFIRMED' and t.operationDate between :start and :end group by t.category.name,t.type order by t.category.name")
    java.util.List<CashFlowRow> cashFlow(UUID departmentId, String currency, LocalDate start, LocalDate end);

    interface CashFlowRow {
        String getCategory();

        org.example.finance.transaction.model.TransactionType getType();

        BigDecimal getAmount();
    }
}
