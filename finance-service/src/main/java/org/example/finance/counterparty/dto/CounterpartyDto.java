package org.example.finance.counterparty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.finance.counterparty.model.CounterpartyType;

import java.util.UUID;

public record CounterpartyDto(UUID id, @NotBlank String name, String taxIdentifier, @NotNull CounterpartyType type,
                              boolean active) {
}
