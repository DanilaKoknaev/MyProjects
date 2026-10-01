package org.example.payment.kafka;

import org.example.payment.config.KafkaTopics;
import org.example.payment.kafka.dto.OrderCreatedEvent;
import org.example.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {
    private final PaymentService paymentService;

    public OrderCreatedConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = KafkaTopics.NEW_ORDERS, groupId = "payment-service")
    public void process(OrderCreatedEvent order) {
        paymentService.process(order);
    }
}
