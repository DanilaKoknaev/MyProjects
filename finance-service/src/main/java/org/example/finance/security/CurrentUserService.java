package org.example.finance.security;

import org.example.finance.common.exception.NotFoundException;
import org.example.finance.user.model.Role;
import org.example.finance.user.model.User;
import org.example.finance.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {
    private final UserRepository users;

    public CurrentUserService(UserRepository users) {
        this.users = users;
    }

    public User get() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmailIgnoreCase(email).orElseThrow(() -> new NotFoundException("Current user"));
    }

    public boolean seesAll(User u) {
        return u.getRole() == Role.ADMIN || u.getRole() == Role.FINANCE_MANAGER || u.getRole() == Role.AUDITOR;
    }

    public void requireDepartment(User u, UUID departmentId) {
        if (!seesAll(u) && (u.getDepartment() == null || !u.getDepartment().getId().equals(departmentId)))
            throw new org.springframework.security.access.AccessDeniedException("Department access denied");
    }
}
