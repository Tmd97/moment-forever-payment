package com.forvmom.MomentForeverPayment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.events.PaymentRequestedEvent;
import com.forvmom.MomentForeverPayment.repository.PaymentOutboxDao;
import com.forvmom.MomentForeverPayment.repository.OutgoingPaymentOutboxDao;
import com.forvmom.MomentForeverPayment.scheduler.OutgoingPaymentPublisher;
import com.forvmom.MomentForeverPayment.scheduler.PaymentDeadLetterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentIncomingRetryService {

    private static final Logger log = LoggerFactory.getLogger(PaymentIncomingRetryService.class);

    @Value("${payment.retry.max-retries:5}")
    private int maxRetries;

    @Value("${payment.retry.incoming.grace-period-minutes:2}")
    private long gracePeriodMinutes;

    private final PaymentOutboxDao outboxDao;
    private final OutgoingPaymentOutboxDao outgoingDao;
    private final PaymentOutboxService outboxService;
    private final PaymentService paymentService;
    private final OutgoingPaymentPublisher outgoingPublisher;
    private final ObjectMapper objectMapper;
    private final PaymentDeadLetterHandler deadLetterHandler;

    public PaymentIncomingRetryService(PaymentOutboxDao outboxDao,
                                       OutgoingPaymentOutboxDao outgoingDao,
                                       PaymentOutboxService outboxService,
                                       PaymentService paymentService,
                                       OutgoingPaymentPublisher outgoingPublisher,
                                       ObjectMapper objectMapper,
                                       PaymentDeadLetterHandler deadLetterHandler) {
        this.outboxDao = outboxDao;
        this.outgoingDao = outgoingDao;
        this.outboxService = outboxService;
        this.paymentService = paymentService;
        this.outgoingPublisher = outgoingPublisher;
        this.objectMapper = objectMapper;
        this.deadLetterHandler = deadLetterHandler;
    }

    /**
     * Retry stuck payment requests (PAYMENT_REQUESTED events)
     * Called by Quartz every 2 minutes
     */
    @Transactional
    public void retryStuckAndFailedRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(gracePeriodMinutes);

        // Find stuck payment requests (incoming)
        List<PaymentOutbox> stuck = outboxDao.findByStatusInAndUpdatedAtBefore(
                List.of(
                        PaymentOutbox.STATUS_PROCESSING,  // Stuck during processing
                        PaymentOutbox.STATUS_FAILED,      // Previously failed
                        PaymentOutbox.STATUS_PENDING      // Never processed
                ),
                cutoff);

        if (stuck.isEmpty()) {
            return;
        }

        log.info("Found {} stuck payment requests to retry", stuck.size());

        for (PaymentOutbox outbox : stuck) {
            // Skip if already processed (safety check)
            if (PaymentOutbox.STATUS_PROCESSED.equals(outbox.getStatus())) {
                continue;
            }

            // Check max retries
            if (outbox.getRetryCount() >= maxRetries) {
                deadLetterHandler.handleDeadRecordForIncomingEvents(outbox);
                continue;
            }

            try {
                // Increment retry count and mark as PROCESSING
                outboxService.incrementRetry(outbox);

                String bookingId = outbox.getBookingId();

                // BRANCH A: Check if outgoing record already exists (payment already processed)
                Optional<OutgoingPaymentOutbox> existingOutgoing = outgoingDao
                        .findByBookingIdAndEventType(bookingId, PaymentService.EVT_PAYMENT_PROCESSED);

                if (existingOutgoing.isPresent()) {
                    // Payment was already processed, just need to publish
                    OutgoingPaymentOutbox outgoing = existingOutgoing.get();
                    if (!OutgoingPaymentOutbox.STATUS_SENT.equals(outgoing.getStatus())) {
                        log.info("[Branch A] Republishing existing processed payment for bookingId={}", bookingId);
                        outgoingPublisher.trySinglePublish(outgoing);
                    }
                    outboxService.markAsProcessed(outbox);
                    continue;
                }

                // BRANCH B: Check if failed record exists
                Optional<OutgoingPaymentOutbox> existingFailed = outgoingDao
                        .findByBookingIdAndEventType(bookingId, PaymentService.EVT_PAYMENT_FAILED);

                if (existingFailed.isPresent()) {
                    // Payment already failed, just need to publish
                    OutgoingPaymentOutbox outgoing = existingFailed.get();
                    if (!OutgoingPaymentOutbox.STATUS_SENT.equals(outgoing.getStatus())) {
                        log.info("[Branch A] Republishing existing failed payment for bookingId={}", bookingId);
                        outgoingPublisher.trySinglePublish(outgoing);
                    }
                    outboxService.markAsProcessed(outbox);
                    continue;
                }

                // BRANCH C: No outgoing record - reprocess the payment
                log.info("[Branch B] Reprocessing payment for bookingId={}", bookingId);

                // Deserialize the original event
                PaymentRequestedEvent event = objectMapper.readValue(
                        outbox.getPayload(),
                        PaymentRequestedEvent.class
                );

                // Process payment again (this will create a new outgoing record)
                OutgoingPaymentOutbox newOutgoing = paymentService.processPayment(event);

                // Mark incoming as processed
                outboxService.markAsProcessed(outbox);

                // Attempt to publish the result
                if (newOutgoing != null) {
                    outgoingPublisher.trySinglePublish(newOutgoing);
                }

                log.info("Successfully retried payment for bookingId={}", bookingId);

            } catch (Exception e) {
                log.error("Retry failed for payment outbox id={}, bookingId={}",
                        outbox.getId(), outbox.getBookingId(), e);
                outboxService.markAsFailed(outbox);
            }
        }
    }
}