package com.forvmom.MomentForeverPayment.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.events.PaymentFailedEvent;
import com.forvmom.MomentForeverPayment.events.PaymentProcessedEvent;
import com.forvmom.MomentForeverPayment.service.AlertService;
import com.forvmom.MomentForeverPayment.service.OutgoingPaymentOutboxService;
import com.forvmom.MomentForeverPayment.service.PaymentOutboxService;
import com.forvmom.MomentForeverPayment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentDeadLetterHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentDeadLetterHandler.class);

    private final OutgoingPaymentOutboxService outgoingService;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;
    private final PaymentOutboxService paymentOutboxService;

    public PaymentDeadLetterHandler(OutgoingPaymentOutboxService outgoingService,
                                    PaymentOutboxService paymentOutboxService,
                                    AlertService alertService,
                                    ObjectMapper objectMapper) {
        this.outgoingService = outgoingService;
        this.alertService = alertService;
        this.objectMapper = objectMapper;
        this.paymentOutboxService=paymentOutboxService;
    }

    public void handleDeadRecordForOutgoingEvents(OutgoingPaymentOutbox record) {
        outgoingService.markAsDead(record);

        String eventType = record.getEventType();
        String bookingId = record.getBookingId();

        String msg = String.format(
                "🔴 PAYMENT DEAD LETTER: id=%d, type=%s, bookingId=%s, retries=%d",
                record.getId(), eventType, bookingId, record.getRetryCount()
        );

        log.error(msg);
        alertService.sendAlert(msg);

        // Compensation logic
        try {
            compensateDeadRecordForOutgoingEvents(record);
        } catch (Exception e) {
            log.error("Compensation failed for dead record id={}", record.getId(), e);
            alertService.sendAlert("Compensation failed for dead record " + record.getId());
        }
    }

    public void handleDeadRecordForIncomingEvents(PaymentOutbox record) {
        paymentOutboxService.markAsDead(record);

        String eventType = record.getEventType();
        String bookingId = record.getBookingId();

        String msg = String.format(
                "🔴 PAYMENT DEAD LETTER: id=%d, type=%s, bookingId=%s, retries=%d",
                record.getId(), eventType, bookingId, record.getRetryCount()
        );

        log.error(msg);
        alertService.sendAlert(msg);

        // Compensation logic
        try {
            compensateDeadRecordForIncomingEvents(record);
        } catch (Exception e) {
            log.error("Compensation failed for dead record id={}", record.getId(), e);
            alertService.sendAlert("Compensation failed for dead record " + record.getId());
        }
    }

    private void compensateDeadRecordForOutgoingEvents(OutgoingPaymentOutbox record) throws Exception {
        String eventType = record.getEventType();
        String payload = record.getPayload();

        switch (eventType) {
            case PaymentService.EVT_PAYMENT_PROCESSED:
                PaymentProcessedEvent processed = objectMapper.readValue(payload, PaymentProcessedEvent.class);
                log.warn("Dead PAYMENT_PROCESSED for booking {} - manual refund may be needed",
                        processed.getBookingId());
                alertService.sendAlert(String.format(
                        "MANUAL ACTION: Refund may be needed for booking %s, transaction %s",
                        processed.getBookingId(), processed.getTransactionId()
                ));
                break;

            case PaymentService.EVT_PAYMENT_FAILED:
                PaymentFailedEvent failed = objectMapper.readValue(payload, PaymentFailedEvent.class);
                log.warn("Dead PAYMENT_FAILED for booking {} - notification to booking service failed",
                        failed.getBookingId());
                // Payment failure notification is less critical
                break;

            default:
                log.warn("No compensation for dead event type: {}", eventType);
        }
    }

    private void compensateDeadRecordForIncomingEvents(PaymentOutbox record) throws Exception {
        String eventType = record.getEventType();
        String payload = record.getPayload();

        switch (eventType) {
            case PaymentService.EVT_PAYMENT_PROCESSED:
                PaymentProcessedEvent processed = objectMapper.readValue(payload, PaymentProcessedEvent.class);
                log.warn("Dead PAYMENT_PROCESSED for booking {} - manual refund may be needed",
                        processed.getBookingId());
                alertService.sendAlert(String.format(
                        "MANUAL ACTION: Refund may be needed for booking %s, transaction %s",
                        processed.getBookingId(), processed.getTransactionId()
                ));
                break;

            case PaymentService.EVT_PAYMENT_FAILED:
                PaymentFailedEvent failed = objectMapper.readValue(payload, PaymentFailedEvent.class);
                log.warn("Dead PAYMENT_FAILED for booking {} - notification to booking service failed",
                        failed.getBookingId());
                // Payment failure notification is less critical
                break;

            default:
                log.warn("No compensation for dead event type: {}", eventType);
        }
    }
}