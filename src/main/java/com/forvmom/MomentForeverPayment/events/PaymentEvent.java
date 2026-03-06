package com.forvmom.MomentForeverPayment.events;

public interface PaymentEvent {
    String getEventType();
    String getBookingId();
}