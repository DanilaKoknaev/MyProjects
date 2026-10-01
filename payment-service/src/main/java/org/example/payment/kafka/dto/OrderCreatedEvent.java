package org.example.payment.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID eventId, String eventType, int eventVersion, Instant occurredAt,
                                UUID orderId, String userId, List<OrderItemEvent> items,
                                BigDecimal totalAmount, String currency) {
}
