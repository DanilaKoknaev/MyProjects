package org.example.order.controller;

import jakarta.validation.Valid;
import org.example.order.dto.CreateOrderRequest;
import org.example.order.dto.OrderResponse;
import org.example.order.dto.UpdateOrderStatusRequest;
import org.example.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id,
                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return service.updateStatus(id, request.status());
    }
}
