package org.example.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String userId,
        @NotEmpty List<@Valid ItemRequest> items,
        @NotNull @DecimalMin("0.01") BigDecimal totalAmount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency
) {
    public record ItemRequest(@NotBlank String productId, @Min(1) int quantity,
                              @NotNull @DecimalMin("0.01") BigDecimal price) {
    }
}
