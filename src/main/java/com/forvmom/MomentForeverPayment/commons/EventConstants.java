package com.forvmom.MomentForeverPayment.commons;

public final class EventConstants {

    private EventConstants() {
        // prevent instantiation
    }

    //============OUTBOX STATUS===========================


    public static final String PENDING = "PENDING";
    public static final String FAILED = "FAILED";
    public static final String PROCESSED = "PROCESSED";
    public static final String PROCESSING = "PROCESSING";
    public static final String DEAD = "DEAD";

    // ==========================================================
    // PAYMENT EVENTS
    // ==========================================================

    public static final String PAYMENT_REQUESTED = "PAYMENT_REQUESTED";
    public static final String PAYMENT_PROCESSED = "PAYMENT_PROCESSED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PAYMENT_REFUNDED = "PAYMENT_REFUNDED";


    // ==========PAYMENT TYPES===========================
    public static final String CREDIT_CARD = "CREDIT_CARD";
    public static final String PAYPAL = "PAYPAL";
    public static final String UPI = "UPI";




}