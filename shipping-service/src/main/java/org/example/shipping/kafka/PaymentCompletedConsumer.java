package org.example.shipping.kafka;

import org.example.shipping.config.KafkaTopics;
import org.example.shipping.kafka.dto.PaymentCompletedEvent;
import org.example.shipping.service.ShippingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedConsumer {
    private final ShippingService shippingService;

    public PaymentCompletedConsumer(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @KafkaListener(topics = KafkaTopics.PAID_ORDERS, groupId = "shipping-service")
    public void process(PaymentCompletedEvent payment) {
        shippingService.process(payment);
    }
}
