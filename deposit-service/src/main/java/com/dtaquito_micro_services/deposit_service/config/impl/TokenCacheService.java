package com.dtaquito_micro_services.deposit_service.config.impl;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenCacheService {

    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    public void storeToken(String transactionId, String token) {
        tokenCache.put(transactionId, token);
    }

    public String getToken(String transactionId) {
        return tokenCache.get(transactionId);
    }

    public void removeToken(String transactionId) {
        tokenCache.remove(transactionId);
    }

    public String generateAndStoreToken(String transactionId) {
        String token = UUID.randomUUID().toString(); // Generate a simple token for demonstration
        storeToken(transactionId, token);
        return token;
    }
}