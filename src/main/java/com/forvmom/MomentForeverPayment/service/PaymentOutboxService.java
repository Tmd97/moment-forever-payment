package com.forvmom.MomentForeverPayment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.events.PaymentEvent;
import com.forvmom.MomentForeverPayment.repository.PaymentOutboxDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentOutboxService {

    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxService.class);

    private final PaymentOutboxDao outboxDao;
    private final ObjectMapper objectMapper;

    public PaymentOutboxService(PaymentOutboxDao outboxDao, ObjectMapper objectMapper) {
        this.outboxDao = outboxDao;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentOutbox findOrCreateForEvent(PaymentEvent event) {
        String bookingId = event.getBookingId();
        String eventType = event.getEventType();

        try {
            Optional<PaymentOutbox> existing = outboxDao.findByBookingIdAndEventType(bookingId, eventType);

            if (existing.isPresent()) {
                log.debug("Found existing outbox record for bookingId={}, eventType={}", bookingId, eventType);
                return existing.get();
            }

            return createNewOutbox(event);

        } catch (DataIntegrityViolationException e) {
            // Race condition - another thread inserted concurrently
            log.warn("Concurrent insert detected for bookingId={}, eventType={}, fetching existing",
                    bookingId, eventType);
            return outboxDao.findByBookingIdAndEventType(bookingId, eventType)
                    .orElseThrow(() -> new IllegalStateException("Failed to recover from concurrent insert", e));
        }
    }

    private PaymentOutbox createNewOutbox(PaymentEvent event) {
        try {
            PaymentOutbox outbox = new PaymentOutbox();
            outbox.setBookingId(event.getBookingId());
            outbox.setEventType(event.getEventType());
            outbox.setStatus(PaymentOutbox.STATUS_PENDING);
            outbox.setRetryCount(0);
            outbox.setPayload(objectMapper.writeValueAsString(event));

            PaymentOutbox saved = outboxDao.save(outbox);
            log.info("Created new outbox record id={} for bookingId={}, eventType={}",
                    saved.getId(), event.getBookingId(), event.getEventType());
            return saved;

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload for bookingId={}", event.getBookingId(), e);
            throw new RuntimeException("Failed to create outbox record", e);
        }
    }

    @Transactional
    public void markAsProcessing(PaymentOutbox outbox) {
        outbox.setStatus(PaymentOutbox.STATUS_PROCESSING);
        outboxDao.save(outbox);
        log.debug("Marked outbox id={} as PROCESSING", outbox.getId());
    }

    @Transactional
    public void markAsProcessed(PaymentOutbox outbox) {
        outbox.setStatus(PaymentOutbox.STATUS_PROCESSED);
        outboxDao.save(outbox);
        log.debug("Marked outbox id={} as PROCESSED", outbox.getId());
    }

    @Transactional
    public void markAsFailed(PaymentOutbox outbox) {
        outbox.setStatus(PaymentOutbox.STATUS_FAILED);
        outboxDao.save(outbox);
        log.debug("Marked outbox id={} as FAILED", outbox.getId());
    }

    @Transactional
    public void markAsDead(PaymentOutbox outbox) {
        outbox.setStatus(PaymentOutbox.STATUS_DEAD);
        outboxDao.save(outbox);
        log.error("Marked outbox id={} as DEAD after max retries", outbox.getId());
    }

    @Transactional
    public void incrementRetry(PaymentOutbox outbox) {
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outbox.setStatus(PaymentOutbox.STATUS_PROCESSING);
        outboxDao.save(outbox);
        log.debug("Incremented retry count to {} for outbox id={}", outbox.getRetryCount(), outbox.getId());
    }
}