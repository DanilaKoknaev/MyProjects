package org.example.finance.account.controller;

import jakarta.validation.Valid;
import org.example.finance.account.dto.AccountDto;
import org.example.finance.account.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService s) {
        service = s;
    }

    @GetMapping
    public Page<AccountDto> list(Pageable p) {
        return service.list(p);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public AccountDto create(@Valid @RequestBody AccountDto d) {
        return service.create(d);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public AccountDto update(@PathVariable UUID id, @Valid @RequestBody AccountDto d) {
        return service.update(id, d);
    }
}
