package org.example.payment.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(UUID eventId, String eventType, int eventVersion, Instant occurredAt,
                                    UUID orderId, String userId, UUID paymentId,
                                    BigDecimal amount, String currency) {
}
