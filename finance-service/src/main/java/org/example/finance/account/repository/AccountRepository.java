package org.example.finance.account.repository;

import org.example.finance.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Page<Account> findByDepartmentId(UUID departmentId, Pageable pageable);
}
