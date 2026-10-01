package org.example.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order.kafka.dto.OrderCreatedEvent;
import org.example.order.kafka.dto.OrderItemEvent;
import org.example.order.dto.CreateOrderRequest;
import org.example.order.dto.OrderResponse;
import org.example.order.dto.OrderItemResponse;
import org.example.order.entity.OrderEntity;
import org.example.order.entity.OrderStatus;
import org.example.order.kafka.OrderEventProducer;
import org.example.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository repository;
    private final ObjectMapper objectMapper;
    private final OrderEventProducer eventProducer;

    public OrderService(OrderRepository repository, ObjectMapper objectMapper, OrderEventProducer eventProducer) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();
        List<OrderItemEvent> items = request.items().stream()
                .map(i -> new OrderItemEvent(i.productId(), i.quantity(), i.price())).toList();
        try {
            OrderEntity entity = repository.save(new OrderEntity(orderId, request.userId(),
                    objectMapper.writeValueAsString(items), request.totalAmount(), request.currency(),
                    OrderStatus.CREATED, now, now));
            OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), "ORDER_CREATED", 1, now,
                    orderId, request.userId(), items, request.totalAmount(), request.currency());
            eventProducer.sendOrderCreated(event);
            log.info("event=order_created orderId={} userId={}", orderId, request.userId());
            return toResponse(entity);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize order items", e);
        }
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus status) {
        OrderEntity entity = find(id);
        entity.changeStatus(status);
        log.info("event=order_status_changed orderId={} status={}", id, status);
        return toResponse(entity);
    }

    private OrderEntity find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private OrderResponse toResponse(OrderEntity entity) {
        try {
            List<OrderItemEvent> storedItems = objectMapper.readValue(entity.getItemsJson(), new TypeReference<>() {
            });
            List<OrderItemResponse> items = storedItems.stream()
                    .map(item -> new OrderItemResponse(item.productId(), item.quantity(), item.price()))
                    .toList();
            return new OrderResponse(entity.getId(), entity.getUserId(), items, entity.getTotalAmount(),
                    entity.getCurrency(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot deserialize order items", e);
        }
    }
}
