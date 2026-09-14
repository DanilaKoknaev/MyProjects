package org.example.finance.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.finance.user.model.Role;

import java.util.UUID;

public record UpdateUserRequest(@NotBlank String fullName, @NotNull Role role, UUID departmentId, boolean enabled) {
}
