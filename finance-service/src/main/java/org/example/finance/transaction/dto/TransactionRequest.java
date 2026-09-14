package org.example.finance.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.example.finance.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(@NotNull TransactionType type,
                                 @NotNull @DecimalMin(value = "0.0001") BigDecimal amount,
                                 @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
                                 @NotNull LocalDate operationDate, @NotNull UUID sourceAccountId,
                                 UUID destinationAccountId, UUID categoryId, UUID counterpartyId) {
}
