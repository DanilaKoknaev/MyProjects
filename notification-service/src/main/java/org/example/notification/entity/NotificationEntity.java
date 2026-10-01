package org.example.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    private UUID id;
    private UUID eventId;
    private UUID orderId;
    private String userId;
    private String message;
    private Instant sentAt;

    protected NotificationEntity() {
    }

    public NotificationEntity(UUID id, UUID eventId, UUID orderId, String userId,
                              String message, Instant sentAt) {
        this.id = id;
        this.eventId = eventId;
        this.orderId = orderId;
        this.userId = userId;
        this.message = message;
        this.sentAt = sentAt;
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getMessage() { return message; }
    public Instant getSentAt() { return sentAt; }
}
