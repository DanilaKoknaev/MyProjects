package org.example.shipping.service;

import org.example.shipping.kafka.dto.OrderShippedEvent;
import org.example.shipping.kafka.dto.PaymentCompletedEvent;
import org.example.shipping.entity.ShipmentEntity;
import org.example.shipping.kafka.ShippingEventProducer;
import org.example.shipping.repository.ProcessedEventRepository;
import org.example.shipping.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ShippingService {
    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);
    private final ProcessedEventRepository processedEvents;
    private final ShipmentRepository shipmentRepository;
    private final ShippingEventProducer eventProducer;

    public ShippingService(ProcessedEventRepository processedEvents, ShipmentRepository shipmentRepository,
                           ShippingEventProducer eventProducer) {
        this.processedEvents = processedEvents;
        this.shipmentRepository = shipmentRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public void process(PaymentCompletedEvent payment) {
        if (!processedEvents.claim(payment.eventId())) {
            log.info("event=duplicate_skipped eventId={} orderId={}", payment.eventId(), payment.orderId());
            return;
        }
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        UUID shipmentId = UUID.randomUUID();
        Instant shippedAt = Instant.now();
        log.info("event=order_packing orderId={}", payment.orderId());
        shipmentRepository.save(new ShipmentEntity(shipmentId, payment.orderId(), payment.userId(),
                trackingNumber, "SHIPPED", shippedAt));
        var event = new OrderShippedEvent(UUID.randomUUID(), "ORDER_SHIPPED", 1,
                shippedAt, payment.orderId(), payment.userId(), trackingNumber);
        eventProducer.sendOrderShipped(event);
        log.info("event=order_shipped orderId={} trackingNumber={}", payment.orderId(), trackingNumber);
    }
}
