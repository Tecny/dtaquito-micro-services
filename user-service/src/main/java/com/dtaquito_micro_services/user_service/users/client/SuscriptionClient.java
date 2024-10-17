package com.dtaquito_micro_services.user_service.users.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class SuscriptionClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    public ResponseEntity<String> createSubscription(Long userId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("subscription-service");
        if (instances == null || instances.isEmpty()) {
            throw new IllegalStateException("No instances of suscription-service found");
        }
        String url = instances.get(0).getUri().toString() + "/api/v1/suscriptions/create/sub?userId=" + userId;
        HttpEntity<Void> requestEntity = new HttpEntity<>(null, new HttpHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }
}