package org.example.finance.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.finance.user.model.Role;

import java.util.UUID;

public record CreateUserRequest(@Email @NotBlank String email, @Size(min = 10, max = 100) String password,
                                @NotBlank String fullName, @NotNull Role role, UUID departmentId) {
}
