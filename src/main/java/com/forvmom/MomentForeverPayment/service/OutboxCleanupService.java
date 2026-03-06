package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.repository.PaymentOutboxDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(OutboxCleanupService.class);
    private static final int HOURS_TO_KEEP = 24;

    private final PaymentOutboxDao paymentOutboxDao;

    public OutboxCleanupService(PaymentOutboxDao paymentOutboxDao) {
        this.paymentOutboxDao = paymentOutboxDao;
    }

    // Cleanup old PROCESSED records
    @Transactional
    public void cleanupPublishedRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(HOURS_TO_KEEP);
        int deleted = paymentOutboxDao.deleteByStatusAndUpdatedAtBefore(PaymentOutbox.STATUS_PROCESSED, cutoff);
        if (deleted > 0) {
            logger.info("Cleaned up {} processed records older than {}", deleted, cutoff);
        }
    }
}