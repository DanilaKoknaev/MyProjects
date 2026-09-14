package org.example.finance.audit.repository;

import org.example.finance.audit.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import org.example.finance.audit.model.AuditAction;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    long countByAction(AuditAction action);
}
