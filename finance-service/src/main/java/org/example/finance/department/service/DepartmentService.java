package org.example.finance.department.service;

import org.example.finance.common.exception.NotFoundException;
import org.example.finance.department.dto.DepartmentDto;
import org.example.finance.department.model.Department;
import org.example.finance.department.repository.DepartmentRepository;
import org.example.finance.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class DepartmentService {
    private final DepartmentRepository repository;
    private final UserRepository users;

    public DepartmentService(DepartmentRepository r, UserRepository u) {
        repository = r;
        users = u;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentDto create(DepartmentDto d) {
        var e = new Department(d.code(), d.name());
        setResponsible(e, d.responsibleUserId());
        return dto(repository.save(e));
    }

    @Transactional(readOnly = true)
    public Page<DepartmentDto> list(Pageable p) {
        return repository.findAll(p).map(this::dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentDto update(UUID id, DepartmentDto d) {
        var e = get(id);
        e.update(d.code(), d.name());
        setResponsible(e, d.responsibleUserId());
        return dto(e);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(UUID id) {
        repository.delete(get(id));
    }

    private Department get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Department"));
    }

    private void setResponsible(Department e, UUID id) {
        e.setResponsible(id == null ? null : users.findById(id).orElseThrow(() -> new NotFoundException("User")));
    }

    private DepartmentDto dto(Department e) {
        return new DepartmentDto(e.getId(), e.getCode(), e.getName(), e.getResponsible() == null ? null : e.getResponsible().getId());
    }
}
