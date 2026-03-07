package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.commons.EventConstants;
import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentResult;
import com.forvmom.MomentForeverPayment.events.InboundPaymentEvent;
import com.forvmom.MomentForeverPayment.events.OutGoingEvent;
import com.forvmom.MomentForeverPayment.events.PaymentFailedEvent;
import com.forvmom.MomentForeverPayment.events.PaymentProcessedEvent;
import com.forvmom.MomentForeverPayment.producer.PaymentEventProducer;
import com.forvmom.MomentForeverPayment.repository.OutgoingPaymentOutboxDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentFailedEventPublisher implements PaymentEventOutgoingStrategy {


    @Autowired
    public PaymentEventProducer paymentEventProducer;

    @Autowired
    private OutgoingPaymentOutboxService outgoingPaymentOutboxService;

    @Override
    public OutgoingPaymentOutbox processOutgoingEvent(PaymentOutbox paymentOutbox, InboundPaymentEvent inboundPaymentEvent, PaymentResult paymentResult) {
        PaymentFailedEvent paymentFailedEvent=new PaymentFailedEvent();
        paymentFailedEvent.setBookingId(inboundPaymentEvent.getBookingId());
        return outgoingPaymentOutboxService.createRecord(paymentOutbox.getBookingId(), paymentFailedEvent.getEventType(), paymentOutbox.getPayload());
    }

    @Override
    public String getSupportedEventType() {
        return EventConstants.PAYMENT_FAILED;
    }
}
