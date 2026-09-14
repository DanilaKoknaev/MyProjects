package org.example.finance.audit.dto;

import org.example.finance.audit.model.AuditAction;

import java.time.Instant;
import java.util.UUID;

public record AuditEventDto(UUID id, UUID actorId,
                            UUID transactionId,
                            AuditAction action,
                            String result,
                            String details, Instant occurredAt) {
}
