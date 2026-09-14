package org.example.finance.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.example.finance.account.model.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto(UUID id, @NotBlank String name, @NotNull AccountType type,
                         @Pattern(regexp = "[A-Z]{3}") String currency, @NotNull UUID departmentId, boolean active,
                         BigDecimal balance) {
}
