package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.repository.OutgoingPaymentOutboxDao;
import com.forvmom.MomentForeverPayment.repository.PaymentOutboxDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxOutgoingCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(OutboxCleanupService.class);
    private static final int HOURS_TO_KEEP = 24;

    private final OutgoingPaymentOutboxDao outgoingPaymentOutboxDao;


    public OutboxOutgoingCleanupService(OutgoingPaymentOutboxDao outgoingPaymentOutboxDao) {
        this.outgoingPaymentOutboxDao = outgoingPaymentOutboxDao;
    }

    // Cleanup old PROCESSED records
    @Transactional
    public void cleanupPublishedRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(HOURS_TO_KEEP);
        int deleted = outgoingPaymentOutboxDao.deleteByStatusAndUpdatedAtBefore(PaymentOutbox.STATUS_PROCESSED, cutoff);
        if (deleted > 0) {
            logger.info("Cleaned up {} processed records older than {}", deleted, cutoff);
        }
    }
}