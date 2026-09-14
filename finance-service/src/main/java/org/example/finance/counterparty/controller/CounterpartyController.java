package org.example.finance.counterparty.controller;

import jakarta.validation.Valid;
import org.example.finance.counterparty.dto.CounterpartyDto;
import org.example.finance.counterparty.service.CounterpartyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/counterparties")
public class CounterpartyController {
    private final CounterpartyService service;

    public CounterpartyController(CounterpartyService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CounterpartyDto> list(Pageable p) {
        return service.list(p);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public CounterpartyDto create(@Valid @RequestBody CounterpartyDto d) {
        return service.create(d);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public CounterpartyDto update(@PathVariable UUID id, @Valid @RequestBody CounterpartyDto d) {
        return service.update(id, d);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
