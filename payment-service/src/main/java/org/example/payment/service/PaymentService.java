package org.example.payment.service;

import org.example.payment.kafka.dto.OrderCreatedEvent;
import org.example.payment.kafka.dto.PaymentCompletedEvent;
import org.example.payment.entity.PaymentEntity;
import org.example.payment.kafka.PaymentEventProducer;
import org.example.payment.repository.ProcessedEventRepository;
import org.example.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final ProcessedEventRepository processedEvents;
    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer eventProducer;

    public PaymentService(ProcessedEventRepository processedEvents, PaymentRepository paymentRepository,
                          PaymentEventProducer eventProducer) {
        this.processedEvents = processedEvents;
        this.paymentRepository = paymentRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public void process(OrderCreatedEvent order) {
        if (!processedEvents.claim(order.eventId())) {
            log.info("event=duplicate_skipped eventId={} orderId={}", order.eventId(), order.orderId());
            return;
        }
        UUID paymentId = UUID.randomUUID();
        Instant completedAt = Instant.now();
        log.info("event=payment_processing orderId={} amount={} currency={}",
                order.orderId(), order.totalAmount(), order.currency());
        paymentRepository.save(new PaymentEntity(paymentId, order.orderId(), order.userId(),
                order.totalAmount(), order.currency(), "COMPLETED", completedAt));
        var event = new PaymentCompletedEvent(UUID.randomUUID(), "PAYMENT_COMPLETED", 1,
                completedAt, order.orderId(), order.userId(), paymentId,
                order.totalAmount(), order.currency());
        eventProducer.sendPaymentCompleted(event);
        log.info("event=payment_completed orderId={} paymentId={}", order.orderId(), paymentId);
    }
}
