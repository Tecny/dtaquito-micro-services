package com.dtaquito_micro_services.payments.payments.client;

import com.dtaquito_micro_services.payments.payments.domain.model.valueobjects.PlanTypes;
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
public class SuscriptionClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    public ResponseEntity<?> getSuscriptionsByUserId(Long userId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("subscription-service");

        String url = instances.get(0).getUri().toString() + "/api/v1/suscriptions?userId=" + userId;
        return restTemplate.getForEntity(url, Object.class);
    }

    public ResponseEntity<String> upgradeSubscription(Long userId, PlanTypes newPlanType) {
        List<ServiceInstance> instances = discoveryClient.getInstances("subscription-service");
        String url = instances.get(0).getUri().toString() + "/api/v1/suscriptions/upgrade/subscription?userId=" + userId + "&newPlanType=" + newPlanType;
        HttpEntity<Void> requestEntity = new HttpEntity<>(null, new HttpHeaders());
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
    }
}