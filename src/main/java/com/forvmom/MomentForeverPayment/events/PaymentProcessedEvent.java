package com.forvmom.MomentForeverPayment.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentProcessedEvent implements PaymentEvent {
    private String eventType = "PAYMENT_PROCESSED";
    private String bookingId;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime processedAt;
    private String paymentMethod;

    // Inventory fields needed for compensation
    private String experienceId;
    private String timeSlotMapperId;
    private Integer guestCount;
    private Long userId;
    private String userEmail;

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getBookingId() {
        return bookingId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getExperienceId() {
        return experienceId;
    }

    public void setExperienceId(String experienceId) {
        this.experienceId = experienceId;
    }

    public String getTimeSlotMapperId() {
        return timeSlotMapperId;
    }

    public void setTimeSlotMapperId(String timeSlotMapperId) {
        this.timeSlotMapperId = timeSlotMapperId;
    }

    public Integer getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}