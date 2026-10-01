package org.example.order.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false, columnDefinition = "text")
    private String itemsJson;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected OrderEntity() {
    }

    public OrderEntity(UUID id, String userId, String itemsJson, BigDecimal totalAmount, String currency,
                       OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.itemsJson = itemsJson;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
