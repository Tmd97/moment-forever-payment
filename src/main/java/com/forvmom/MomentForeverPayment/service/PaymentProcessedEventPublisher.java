package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.commons.EventConstants;
import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentResult;
import com.forvmom.MomentForeverPayment.events.InboundPaymentEvent;
import com.forvmom.MomentForeverPayment.events.OutGoingEvent;
import com.forvmom.MomentForeverPayment.events.PaymentProcessedEvent;
import com.forvmom.MomentForeverPayment.producer.PaymentEventProducer;
import com.forvmom.MomentForeverPayment.repository.OutgoingPaymentOutboxDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessedEventPublisher implements PaymentEventOutgoingStrategy {


    @Autowired
    public PaymentEventProducer paymentEventProducer;

    @Autowired
    private OutgoingPaymentOutboxService outgoingPaymentOutboxService;

    @Override
    public OutgoingPaymentOutbox processOutgoingEvent(PaymentOutbox paymentOutbox, InboundPaymentEvent inboundPaymentEvent, PaymentResult paymentResult) {
        PaymentProcessedEvent paymentProcessedEvent=new PaymentProcessedEvent();
        paymentProcessedEvent.setBookingId(inboundPaymentEvent.getBookingId());
        paymentProcessedEvent.setTransactionId(paymentResult.getTransactionId());
        paymentProcessedEvent.setProcessedAt(paymentResult.getCreatedAt());
        return outgoingPaymentOutboxService.createRecord(paymentOutbox.getBookingId(), paymentProcessedEvent.getEventType(), paymentOutbox.getPayload());
    }

    @Override
    public String getSupportedEventType() {
        return EventConstants.PAYMENT_PROCESSED;
    }
}
