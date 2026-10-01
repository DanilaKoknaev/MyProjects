package org.example.order.kafka;

import org.example.order.config.KafkaTopics;
import org.example.order.kafka.dto.OrderShippedEvent;
import org.example.order.kafka.dto.PaymentCompletedEvent;
import org.example.order.entity.OrderStatus;
import org.example.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusConsumer {
    private final OrderService orderService;

    public OrderStatusConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = KafkaTopics.PAID_ORDERS, groupId = "orders-status-payment")
    public void paid(PaymentCompletedEvent event) {
        orderService.updateStatus(event.orderId(), OrderStatus.PAID);
    }

    @KafkaListener(topics = KafkaTopics.SENT_ORDERS, groupId = "orders-status-shipping")
    public void shipped(OrderShippedEvent event) {
        orderService.updateStatus(event.orderId(), OrderStatus.SHIPPED);
    }
}
