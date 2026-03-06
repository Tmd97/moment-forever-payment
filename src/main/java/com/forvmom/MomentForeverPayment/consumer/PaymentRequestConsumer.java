package com.forvmom.MomentForeverPayment.consumer;

import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.events.PaymentRequestedEvent;
import com.forvmom.MomentForeverPayment.scheduler.OutgoingPaymentPublisher;
import com.forvmom.MomentForeverPayment.service.PaymentOutboxService;
import com.forvmom.MomentForeverPayment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestConsumer.class);

    private final PaymentService paymentService;
    private final PaymentOutboxService outboxService;
    private final OutgoingPaymentPublisher outgoingPublisher;

    public PaymentRequestConsumer(PaymentService paymentService,
                                  PaymentOutboxService outboxService,
                                  OutgoingPaymentPublisher outgoingPublisher) {
        this.paymentService = paymentService;
        this.outboxService = outboxService;
        this.outgoingPublisher = outgoingPublisher;
    }

    @KafkaListener(topics = "${kafka.topics.payment-requested}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onPaymentRequested(@Payload PaymentRequestedEvent event, Acknowledgment ack) {
        String bookingId = event.getBookingId();
        log.info("Received payment-requested: bookingId={}, amount={}", bookingId, event.getGrandTotal());

        // Step 1: Idempotency check
        PaymentOutbox outbox = outboxService.findOrCreateForEvent(event);

        if (PaymentOutbox.STATUS_PROCESSED.equals(outbox.getStatus())) {
            log.info("Duplicate payment-requested ignored (already PROCESSED): bookingId={}", bookingId);
            ack.acknowledge();
            return;
        }

        // Step 2: Early ack
        ack.acknowledge();

        OutgoingPaymentOutbox outgoingRecord = null;

        try {
            // Step 3: Mark as processing
            outboxService.markAsProcessing(outbox);

            // Step 4: Process payment (business logic)
            outgoingRecord = paymentService.processPayment(event);

            // Step 5: Mark as processed
            outboxService.markAsProcessed(outbox);

        } catch (Exception e) {
            log.error("Failed to process payment for bookingId={}", bookingId, e);
            outboxService.markAsFailed(outbox);
            return;
        }

        // Step 6: Attempt immediate publish (outside transaction)
        if (outgoingRecord != null) {
            outgoingPublisher.trySinglePublish(outgoingRecord);
        }

        log.info("Payment processing complete for bookingId={}", bookingId);
    }
}