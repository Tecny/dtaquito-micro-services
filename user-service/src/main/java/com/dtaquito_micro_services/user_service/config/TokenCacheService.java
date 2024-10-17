package com.dtaquito_micro_services.user_service.config;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenCacheService {

    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    public void storeToken(String key, String token) {
        tokenCache.put(key, token);
    }

    public String getToken(String key) {
        return tokenCache.get(key);
    }
}