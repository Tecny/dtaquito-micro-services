package com.dtaquito_micro_services.payments.payments.application.internal.commandservices;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentTimestampService {
    private final ConcurrentHashMap<String, LocalDateTime> paymentTimestamps = new ConcurrentHashMap<>();

    public void storeTimestamp(String paymentId) {
        paymentTimestamps.put(paymentId, LocalDateTime.now());
    }

    public LocalDateTime getTimestamp(String paymentId) {
        return paymentTimestamps.get(paymentId);
    }

    public void removeTimestamp(String paymentId) {
        paymentTimestamps.remove(paymentId);
    }
}
