package org.example.order.kafka.dto;

import java.math.BigDecimal;

public record OrderItemEvent(String productId, int quantity, BigDecimal price) {
}
