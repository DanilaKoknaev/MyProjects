package org.example.finance.user.dto;

import org.example.finance.user.model.Role;

import java.util.UUID;

public record UserDto(UUID id, String email, String fullName,
                      Role role, UUID departmentId, boolean enabled) {
}
