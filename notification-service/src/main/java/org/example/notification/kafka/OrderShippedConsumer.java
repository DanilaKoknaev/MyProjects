package org.example.notification.kafka;

import org.example.notification.config.KafkaTopics;
import org.example.notification.kafka.dto.OrderShippedEvent;
import org.example.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderShippedConsumer {
    private final NotificationService notificationService;

    public OrderShippedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaTopics.SENT_ORDERS, groupId = "notification-service")
    public void process(OrderShippedEvent order) {
        notificationService.send(order);
    }
}
