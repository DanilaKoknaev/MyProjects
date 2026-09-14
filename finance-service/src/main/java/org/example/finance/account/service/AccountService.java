package org.example.finance.account.service;

import org.example.finance.account.dto.AccountDto;
import org.example.finance.account.model.Account;
import org.example.finance.account.repository.AccountRepository;
import org.example.finance.common.exception.NotFoundException;
import org.example.finance.department.repository.DepartmentRepository;
import org.example.finance.transaction.repository.TransactionEntryRepository;
import org.example.finance.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class AccountService {
    private final AccountRepository repository;
    private final DepartmentRepository departments;
    private final TransactionEntryRepository entries;
    private final CurrentUserService current;

    public AccountService(AccountRepository r, DepartmentRepository d, TransactionEntryRepository e, CurrentUserService c) {
        repository = r;
        departments = d;
        entries = e;
        current = c;
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public AccountDto create(AccountDto d) {
        var dept = departments.findById(d.departmentId()).orElseThrow(() -> new NotFoundException("Department"));
        return dto(repository.save(new Account(d.name(), d.type(), d.currency(), dept)));
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> list(Pageable p) {
        var u = current.get();
        return (current.seesAll(u) ? repository.findAll(p) : repository.findByDepartmentId(u.getDepartment().getId(), p)).map(this::dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public AccountDto update(UUID id, AccountDto d) {
        var e = get(id);
        e.update(d.name(), d.type(), d.active());
        return dto(e);
    }

    public Account get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Account"));
    }

    public AccountDto dto(Account e) {
        return new AccountDto(e.getId(), e.getName(), e.getType(), e.getCurrency(), e.getDepartment().getId(), e.isActive(), entries.balance(e.getId()));
    }
}
