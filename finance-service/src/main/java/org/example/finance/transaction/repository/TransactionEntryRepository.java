package org.example.finance.transaction.repository;

import org.example.finance.transaction.model.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, UUID> {
    @Query("select coalesce(sum(e.amount), 0) from TransactionEntry e where e.account.id=:accountId and e.transaction.status<>'DRAFT'")
    BigDecimal balance(UUID accountId);
}
