package org.example.order.dto;

import org.example.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, String userId, List<OrderItemResponse> items, BigDecimal totalAmount,
                            String currency, OrderStatus status, Instant createdAt, Instant updatedAt) {
}
