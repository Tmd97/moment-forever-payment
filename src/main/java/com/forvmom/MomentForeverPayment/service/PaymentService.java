package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.events.PaymentFailedEvent;
import com.forvmom.MomentForeverPayment.events.PaymentProcessedEvent;
import com.forvmom.MomentForeverPayment.events.PaymentRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public static final String EVT_PAYMENT_PROCESSED = "PAYMENT_PROCESSED";
    public static final String EVT_PAYMENT_FAILED = "PAYMENT_FAILED";

    @Value("${payment.processing.simulate-failure-rate:0}")
    private int failureRate;

    @Value("${payment.processing.processing-time-ms:500}")
    private int processingTimeMs;

    private final OutgoingPaymentOutboxService outgoingPaymentOutboxService;

    public PaymentService(OutgoingPaymentOutboxService outgoingPaymentOutboxService) {
        this.outgoingPaymentOutboxService = outgoingPaymentOutboxService;
    }

    @Transactional
    public OutgoingPaymentOutbox processPayment(PaymentRequestedEvent event) {
        String bookingId = event.getBookingId();

        // Simulate payment processing delay
        try {
            Thread.sleep(processingTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate random failures
        boolean shouldFail = (failureRate > 0) && (Math.random() * 100 < failureRate);

        if (shouldFail) {
            return handlePaymentFailure(event);
        } else {
            return handlePaymentSuccess(event);
        }
    }

    private OutgoingPaymentOutbox handlePaymentSuccess(PaymentRequestedEvent event) {
        String transactionId = "txn_" + UUID.randomUUID().toString();

        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent();
        processedEvent.setBookingId(event.getBookingId());
        processedEvent.setTransactionId(transactionId);
        processedEvent.setAmount(event.getGrandTotal());
        processedEvent.setCurrency(event.getCurrency());
        processedEvent.setProcessedAt(LocalDateTime.now());
        processedEvent.setPaymentMethod("CREDIT_CARD");

        // Copy inventory fields for compensation
        processedEvent.setExperienceId(event.getExperienceId());
        processedEvent.setTimeSlotMapperId(event.getTimeSlotMapperId());
        processedEvent.setGuestCount(event.getGuestCount());
        processedEvent.setUserId(event.getUserId());
        processedEvent.setUserEmail(event.getUserEmail());

        log.info("Payment SUCCESS for bookingId={}, transactionId={}",
                event.getBookingId(), transactionId);

        return outgoingPaymentOutboxService.createRecord(
                event.getBookingId(),
                EVT_PAYMENT_PROCESSED,
                processedEvent
        );
    }

    private OutgoingPaymentOutbox handlePaymentFailure(PaymentRequestedEvent event) {
        String[] failureReasons = {
                "Insufficient funds",
                "Card declined",
                "Payment gateway timeout",
                "Invalid card details",
                "Bank not responding"
        };

        String randomReason = failureReasons[(int)(Math.random() * failureReasons.length)];

        PaymentFailedEvent failedEvent = new PaymentFailedEvent();
        failedEvent.setBookingId(event.getBookingId());
        failedEvent.setFailureReason(randomReason);
        failedEvent.setErrorCode("PAY_" + (int)(Math.random() * 1000));
        failedEvent.setFailedAt(LocalDateTime.now());
        failedEvent.setAttemptedAmount(event.getGrandTotal());
        failedEvent.setCurrency(event.getCurrency());

        // Copy inventory fields for compensation
        failedEvent.setExperienceId(event.getExperienceId());
        failedEvent.setTimeSlotMapperId(event.getTimeSlotMapperId());
        failedEvent.setGuestCount(event.getGuestCount());
        failedEvent.setUserId(event.getUserId());
        failedEvent.setUserEmail(event.getUserEmail());

        log.warn("Payment FAILED for bookingId={}, reason={}",
                event.getBookingId(), randomReason);

        return outgoingPaymentOutboxService.createRecord(
                event.getBookingId(),
                EVT_PAYMENT_FAILED,
                failedEvent
        );
    }
}