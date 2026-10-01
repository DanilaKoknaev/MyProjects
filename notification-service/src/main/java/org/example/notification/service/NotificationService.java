package org.example.notification.service;

import org.example.notification.kafka.dto.OrderShippedEvent;
import org.example.notification.entity.NotificationEntity;
import org.example.notification.repository.NotificationRepository;
import org.example.notification.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ProcessedEventRepository processedEvents;
    private final NotificationRepository notificationRepository;

    public NotificationService(ProcessedEventRepository processedEvents,
                               NotificationRepository notificationRepository) {
        this.processedEvents = processedEvents;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void send(OrderShippedEvent order) {
        if (!processedEvents.claim(order.eventId())) {
            log.info("event=duplicate_skipped eventId={} orderId={}", order.eventId(), order.orderId());
            return;
        }
        String message = "Order shipped, tracking number: " + order.trackingNumber();
        notificationRepository.save(new NotificationEntity(UUID.randomUUID(), order.eventId(),
                order.orderId(), order.userId(), message, Instant.now()));
        log.info("event=notification_sent userId={} orderId={} message=Order_shipped trackingNumber={}",
                order.userId(), order.orderId(), order.trackingNumber());
    }
}
