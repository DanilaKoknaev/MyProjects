package org.example.finance.department.repository;

import org.example.finance.department.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    boolean existsByCodeIgnoreCase(String code);
}
