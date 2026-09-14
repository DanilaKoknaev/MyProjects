package org.example.finance.transaction.controller;

import jakarta.validation.Valid;
import org.example.finance.transaction.dto.TransactionDto;
import org.example.finance.transaction.dto.TransactionRequest;
import org.example.finance.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public Page<TransactionDto> list(Pageable p) {
        return service.list(p);
    }

    @GetMapping("/{id}")
    public TransactionDto find(@PathVariable UUID id) {
        return service.find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto create(@Valid @RequestBody TransactionRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    public TransactionDto update(@PathVariable UUID id, @Valid @RequestBody TransactionRequest r) {
        return service.update(id, r);
    }

    @PostMapping("/{id}/confirm")
    public TransactionDto confirm(@PathVariable UUID id) {
        return service.confirm(id);
    }

    @PostMapping("/{id}/reverse")
    public TransactionDto reverse(@PathVariable UUID id) {
        return service.reverse(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
