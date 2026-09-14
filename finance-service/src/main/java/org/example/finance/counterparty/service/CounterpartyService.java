package org.example.finance.counterparty.service;

import org.example.finance.common.exception.NotFoundException;
import org.example.finance.counterparty.dto.CounterpartyDto;
import org.example.finance.counterparty.model.Counterparty;
import org.example.finance.counterparty.repository.CounterpartyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class CounterpartyService {
    private final CounterpartyRepository repository;

    public CounterpartyService(CounterpartyRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public CounterpartyDto create(CounterpartyDto d) {
        return dto(repository.save(new Counterparty(d.name(), d.taxIdentifier(), d.type())));
    }

    @Transactional(readOnly = true)
    public Page<CounterpartyDto> list(Pageable p) {
        return repository.findAll(p).map(this::dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public CounterpartyDto update(UUID id, CounterpartyDto d) {
        var e = get(id);
        e.update(d.name(), d.taxIdentifier(), d.type(), d.active());
        return dto(e);
    }

    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_MANAGER')")
    public void delete(UUID id) {
        repository.delete(get(id));
    }

    public Counterparty get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Counterparty"));
    }

    private CounterpartyDto dto(Counterparty e) {
        return new CounterpartyDto(e.getId(), e.getName(), e.getTaxIdentifier(), e.getType(), e.isActive());
    }
}
