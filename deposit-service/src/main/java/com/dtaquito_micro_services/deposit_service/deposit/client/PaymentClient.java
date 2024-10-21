package com.dtaquito_micro_services.deposit_service.deposit.client;

import com.dtaquito_micro_services.deposit_service.config.impl.TokenCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class PaymentClient {

    private static final Logger logger = LoggerFactory.getLogger(PaymentClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenCacheService tokenCacheService;

    @Autowired
    private DiscoveryClient discoveryClient;


    public ResponseEntity<Map<String, String>> createDepositPayment(BigDecimal amount) {
        String token = tokenCacheService.getToken("default");
        if (token == null) {
            throw new IllegalStateException("Token not found");
        }
        List<ServiceInstance> instances = discoveryClient.getInstances("payment-service");

        String url = instances.get(0).getUri().toString() + "/api/v1/payments/create-deposit-payment?amount=" + amount;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});

        if (response.getBody() == null || !response.getBody().containsKey("approval_url")) {
            logger.error("Invalid response from payment service: " + response);
            throw new IllegalStateException("Invalid response from payment service");
        }

        return response;
    }

    public boolean checkPaymentStatus(String userId) {
        String token = tokenCacheService.getToken("default");
        if (token == null) {
            throw new IllegalStateException("Token not found");
        }
        List<ServiceInstance> instances = discoveryClient.getInstances("payment-service");

        String url = instances.get(0).getUri().toString() + "/api/v1/payments/status?userId=" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String status = response.getBody().get("status");
            return "APPROVED".equals(status);
        }

        return false;
    }
}