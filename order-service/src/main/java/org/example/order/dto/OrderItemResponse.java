package org.example.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(String productId, int quantity, BigDecimal price) {
}
