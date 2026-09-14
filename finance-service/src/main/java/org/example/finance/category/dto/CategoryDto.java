package org.example.finance.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.finance.category.model.CategoryType;

import java.util.UUID;

public record CategoryDto(UUID id, @NotBlank String name, @NotNull CategoryType type, UUID parentId, boolean active) {
}
