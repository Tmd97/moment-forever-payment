package com.forvmom.MomentForeverPayment.events;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class PaymentRequestedEvent implements InboundPaymentEvent {
    private String eventType = "PAYMENT_REQUESTED";
    private String bookingId;
    private Long userId;
    private String userEmail;
    private String experienceId;
    private String experienceName;
    private String timeSlotMapperId;
    private Integer guestCount;
    private BigDecimal grandTotal;
    private String currency;
    private LocalDateTime requestedAt;
    private String paymentType; // e.g., "CREDIT_CARD", "PAYPAL"

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getBookingId() {
        return bookingId;
    }

    @Override
    public String getPaymentType() {
        return paymentType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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

    public String getExperienceId() {
        return experienceId;
    }

    public void setExperienceId(String experienceId) {
        this.experienceId = experienceId;
    }

    public String getExperienceName() {
        return experienceName;
    }

    public void setExperienceName(String experienceName) {
        this.experienceName = experienceName;
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

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
