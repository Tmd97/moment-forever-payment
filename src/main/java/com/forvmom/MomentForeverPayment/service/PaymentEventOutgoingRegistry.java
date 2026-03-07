package com.forvmom.MomentForeverPayment.service;

import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentEventOutgoingRegistry {

    private final Map<String, PaymentEventOutgoingStrategy> paymentEventOutgoingStrategyMap;

    public PaymentEventOutgoingRegistry(List<PaymentEventOutgoingStrategy> processors) {
        paymentEventOutgoingStrategyMap = processors.stream()
                .collect(Collectors.toMap(
                        PaymentEventOutgoingStrategy::getSupportedEventType,
                        Function.identity()
                ));
    }


    public PaymentEventOutgoingStrategy paymentEventOutgoingStrategy(boolean status) {
        PaymentEventOutgoingStrategy strategy = paymentEventOutgoingStrategyMap.get(status);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for status type: " + status);
        }
        return strategy;

    }


}
