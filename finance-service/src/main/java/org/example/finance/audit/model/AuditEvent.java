package org.example.finance.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import org.example.finance.common.model.BaseEntity;

import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEvent extends BaseEntity {
    @Column(name = "actor_id")
    private UUID actorId;
    @Column(name = "transaction_id")
    private UUID transactionId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;
    @Column(nullable = false, length = 20)
    private String result;
    @Column(columnDefinition = "text")
    private String details;
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt = Instant.now();

    public AuditEvent(UUID actorId, UUID transactionId, AuditAction action, String result, String details) {
        this.actorId = actorId;
        this.transactionId = transactionId;
        this.action = action;
        this.result = result;
        this.details = details;
    }
}
