package org.example.shipping.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipments")
public class ShipmentEntity {
    @Id
    private UUID id;
    private UUID orderId;
    private String userId;
    private String trackingNumber;
    private String status;
    private Instant createdAt;

    protected ShipmentEntity() {
    }

    public ShipmentEntity(UUID id, UUID orderId, String userId, String trackingNumber,
                          String status, Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
