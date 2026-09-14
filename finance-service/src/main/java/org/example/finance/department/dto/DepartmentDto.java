package org.example.finance.department.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record DepartmentDto(UUID id, @NotBlank String code, @NotBlank String name, UUID responsibleUserId) {
}
