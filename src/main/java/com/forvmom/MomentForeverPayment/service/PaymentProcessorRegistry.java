package com.forvmom.MomentForeverPayment.service;

import com.forvmom.MomentForeverPayment.events.OutGoingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentProcessorRegistry {


    private final Map<String, PaymentStrategy<?>> paymentStrategyMap;

    public PaymentProcessorRegistry(List<PaymentStrategy<?>> processors) {
        paymentStrategyMap = processors.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::getSupportedPaymentType,
                        Function.identity()
                ));
    }


    public PaymentStrategy<?> getPaymentTypeProcessor(String paymentType) {
        PaymentStrategy strategy = paymentStrategyMap.get(paymentType);
        if (strategy == null) {
            throw new IllegalArgumentException("No payment strategy found for type: " + paymentType);
        }
        return strategy;
    }

}
