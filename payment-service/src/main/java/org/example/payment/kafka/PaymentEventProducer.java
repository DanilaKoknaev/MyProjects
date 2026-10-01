package org.example.payment.kafka;

import org.example.payment.config.KafkaTopics;
import org.example.payment.kafka.dto.PaymentCompletedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PaymentEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentCompleted(PaymentCompletedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.PAID_ORDERS, event.orderId().toString(), event)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot publish completed payment", exception);
        }
    }
}
