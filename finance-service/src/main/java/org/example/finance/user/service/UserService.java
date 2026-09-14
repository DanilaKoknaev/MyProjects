package org.example.finance.user.service;

import org.example.finance.common.exception.NotFoundException;
import org.example.finance.department.model.Department;
import org.example.finance.department.repository.DepartmentRepository;
import org.example.finance.user.dto.CreateUserRequest;
import org.example.finance.user.dto.UpdateUserRequest;
import org.example.finance.user.dto.UserDto;
import org.example.finance.user.model.User;
import org.example.finance.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class UserService {
    private final UserRepository repository;
    private final DepartmentRepository departments;
    private final PasswordEncoder encoder;

    public UserService(UserRepository r, DepartmentRepository d, PasswordEncoder e) {
        repository = r;
        departments = d;
        encoder = e;
    }

    public UserDto create(CreateUserRequest r) {
        if (repository.findByEmailIgnoreCase(r.email()).isPresent())
            throw new IllegalStateException("Email already exists");
        return dto(repository.save(new User(r.email(), r.password() == null ? null : encoder.encode(r.password()), r.fullName(), r.role(), department(r.departmentId()))));
    }

    @Transactional(readOnly = true)
    public Page<UserDto> list(Pageable p) {
        return repository.findAll(p).map(this::dto);
    }

    public UserDto update(UUID id, UpdateUserRequest r) {
        var u = get(id);
        u.update(r.fullName(), r.role(), department(r.departmentId()), r.enabled());
        return dto(u);
    }

    public User get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("User"));
    }

    private Department department(UUID id) {
        return id == null ? null : departments.findById(id).orElseThrow(() -> new NotFoundException("Department"));
    }

    private UserDto dto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getDepartment() == null ? null : u.getDepartment().getId(), u.isEnabled());
    }
}
