package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.domain.entity.PaymentResult;
import com.forvmom.MomentForeverPayment.events.InboundPaymentEvent;

public interface PaymentStrategy<T extends InboundPaymentEvent> {
    PaymentResult processPayment(T inboundPaymentEvent);
    String getSupportedPaymentType();
}
