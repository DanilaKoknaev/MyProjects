package org.example.order.kafka.dto;

import java.time.Instant;
import java.util.UUID;

public record OrderShippedEvent(UUID eventId, String eventType, int eventVersion, Instant occurredAt,
                                UUID orderId, String userId, String trackingNumber) {
}
