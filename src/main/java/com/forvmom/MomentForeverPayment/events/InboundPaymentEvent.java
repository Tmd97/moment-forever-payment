package com.forvmom.MomentForeverPayment.events;

public interface InboundPaymentEvent {
    String getEventType();
    String getBookingId();
    String getPaymentType();
}