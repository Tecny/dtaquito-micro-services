package com.dtaquito_micro_services.subscription_service.suscriptions.client;

import com.dtaquito_micro_services.subscription_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.subscription_service.config.impl.UserDetailsImpl;
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

    public ResponseEntity<Map<String, String>> createSubscriptionPayment(BigDecimal amount, String planType) {
        List<ServiceInstance> instances = discoveryClient.getInstances("payment-service");

        String token = tokenCacheService.getToken("default");
        if (token == null) {
            throw new IllegalStateException("Token not found");
        }

        String url = instances.get(0).getUri().toString() + "/api/v1/payments/create-subscription-payment?amount=" + amount + "&planType=" + planType;
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
}