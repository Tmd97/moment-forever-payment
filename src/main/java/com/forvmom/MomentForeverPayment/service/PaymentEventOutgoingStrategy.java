package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.domain.entity.OutgoingPaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentOutbox;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentResult;
import com.forvmom.MomentForeverPayment.events.InboundPaymentEvent;
import com.forvmom.MomentForeverPayment.events.OutGoingEvent;

public interface PaymentEventOutgoingStrategy {
        OutgoingPaymentOutbox processOutgoingEvent(PaymentOutbox paymentOutbox, InboundPaymentEvent inboundPaymentEvent, PaymentResult paymentResult);
        String getSupportedEventType();  // each strategy declares which event type it handles
}
