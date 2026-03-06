package com.forvmom.MomentForeverPayment.producer;


import com.forvmom.MomentForeverPayment.events.PaymentFailedEvent;
import com.forvmom.MomentForeverPayment.events.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.payment-processed}")
    private String paymentProcessedTopic;

    @Value("${kafka.topics.payment-failed}")
    private String paymentFailedTopic;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentProcessedEvent(PaymentProcessedEvent event) {
        kafkaTemplate.send(paymentProcessedTopic, event.getBookingId(), event);
        log.info("Sent PaymentProcessedEvent: bookingId={}, transactionId={}",
                event.getBookingId(), event.getTransactionId());
    }

    public void sendPaymentFailedEvent(PaymentFailedEvent event) {
        kafkaTemplate.send(paymentFailedTopic, event.getBookingId(), event);
        log.info("Sent PaymentFailedEvent: bookingId={}, reason={}",
                event.getBookingId(), event.getFailureReason());
    }
}