package org.example.payment.kafka.dto;

import java.math.BigDecimal;

public record OrderItemEvent(String productId, int quantity, BigDecimal price) {
}
