package org.example.shipping.kafka;

import org.example.shipping.config.KafkaTopics;
import org.example.shipping.kafka.dto.OrderShippedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ShippingEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShippingEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderShipped(OrderShippedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.SENT_ORDERS, event.orderId().toString(), event)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot publish shipped order", exception);
        }
    }
}
