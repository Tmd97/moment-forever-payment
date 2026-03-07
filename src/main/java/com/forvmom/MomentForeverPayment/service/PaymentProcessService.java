package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentResult;
import com.forvmom.MomentForeverPayment.events.InboundPaymentEvent;
import com.forvmom.MomentForeverPayment.producer.PaymentEventProducer;
import com.forvmom.MomentForeverPayment.scheduler.OutgoingPaymentPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessService.class);

    @Autowired
    private PaymentOutboxService outboxService;

    @Autowired
    private PaymentProcessedEventPublisher paymentProcessedEventPublisher;
    @Autowired
    private PaymentProcessorRegistry paymentProcessorRegistry;

    @Autowired
    private PaymentEventOutgoingRegistry paymentEventOutgoingRegistry;

    @Autowired
    private PaymentEventProducer paymentEventProducer;

    @Autowired
    private OutgoingPaymentPublisher outgoingPaymentPublisher;



    public void processPayment(InboundPaymentEvent inboundPaymentEvent, Acknowledgment ack) {
        String bookingId = inboundPaymentEvent.getBookingId();
        log.info("Received payment-requested: bookingId={}, amount={}", bookingId, inboundPaymentEvent);

        // Step 1: Idempotency check
        PaymentOutbox outbox = outboxService.findOrCreateForEvent(inboundPaymentEvent);

        if (PaymentOutbox.STATUS_PROCESSED.equals(outbox.getStatus())) {
            log.info("Duplicate payment-requested ignored (already PROCESSED): bookingId={}", bookingId);
            ack.acknowledge();
            return;
        }
        // Step 2: Early ack
        ack.acknowledge();
        OutgoingPaymentOutbox outgoingRecord = null;
        // Step 3: Mark as processing
        outboxService.markAsProcessing(outbox);

        // Step 4: Process payment (business logic)
        PaymentStrategy paymentStrategy = paymentProcessorRegistry.getPaymentTypeProcessor(inboundPaymentEvent.getPaymentType());
        PaymentResult paymentResult = paymentStrategy.processPayment(inboundPaymentEvent);

       PaymentEventOutgoingStrategy paymentEventOutgoingStrategy= paymentEventOutgoingRegistry.paymentEventOutgoingStrategy(paymentResult.isStatus());

        OutgoingPaymentOutbox outgoingPaymentOutbox = paymentEventOutgoingStrategy.processOutgoingEvent(outbox, inboundPaymentEvent, paymentResult);
        outgoingPaymentPublisher.trySinglePublish(outgoingPaymentOutbox);
        log.info("Payment processing succeeded for bookingId={}", bookingId);

        // Step 5: Mark as processed
        outboxService.markAsProcessed(outbox);
        log.info("Payment processing complete for bookingId={}", bookingId);
    }

}
