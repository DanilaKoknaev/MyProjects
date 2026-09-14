package org.example.finance.audit.service;

import org.example.finance.audit.dto.AuditEventDto;
import org.example.finance.audit.model.AuditAction;
import org.example.finance.audit.model.AuditEvent;
import org.example.finance.audit.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {
    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository r) {
        repository = r;
    }

    @Transactional
    public void record(UUID actor, UUID transaction, AuditAction action, String result, String details) {
        repository.save(new AuditEvent(actor, transaction, action, result, details));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordIndependent(UUID actor, UUID transaction, AuditAction action, String result, String details) {
        repository.save(new AuditEvent(actor, transaction, action, result, details));
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDto> list(Pageable p) {
        return repository.findAll(p).map(this::dto);
    }

    private AuditEventDto dto(AuditEvent e) {
        return new AuditEventDto(e.getId(), e.getActorId(), e.getTransactionId(), e.getAction(), e.getResult(), e.getDetails(), e.getOccurredAt());
    }
}
