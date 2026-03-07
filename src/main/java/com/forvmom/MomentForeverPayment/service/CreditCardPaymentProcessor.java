package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.commons.EventConstants;
import com.forvmom.MomentForeverPayment.domain.entity.PaymentResult;
import com.forvmom.MomentForeverPayment.events.InboundPaymentEvent;
/// ////////////NOTE::: true for success or false for failure////////////////
public class CreditCardPaymentProcessor implements PaymentStrategy<InboundPaymentEvent> {
    @Override
    public PaymentResult processPayment(InboundPaymentEvent inboundPaymentEvent) {
        // Implement credit card payment processing logic here
        // For demonstration, we'll just return a successful payment result
        PaymentResult result = new PaymentResult();
        result.setStatus(true);//  true for success or false for failure
        result.setTransactionId("1234");
        result.setBookingId(inboundPaymentEvent.getBookingId());
        return result;
    }

    @Override
    public String getSupportedPaymentType() {
        return EventConstants.CREDIT_CARD;
    }
}
