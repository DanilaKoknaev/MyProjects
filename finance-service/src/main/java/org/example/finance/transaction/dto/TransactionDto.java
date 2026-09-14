package org.example.finance.transaction.dto;

import org.example.finance.transaction.model.TransactionStatus;
import org.example.finance.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TransactionDto(UUID id, TransactionType type, TransactionStatus status, BigDecimal amount,
                             String currency, LocalDate operationDate, UUID departmentId, UUID categoryId,
                             UUID counterpartyId, UUID authorId, UUID reversalOfId, List<EntryDto> entries) {
    public record EntryDto(UUID accountId, BigDecimal amount) {
    }
}
