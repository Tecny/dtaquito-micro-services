package com.dtaquito_micro_services.sport_service.sportspaces.client;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.entities.Suscriptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
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
        try {
            return restTemplate.getForEntity(url, Object.class);
        } catch (ResourceAccessException e) {
            return getSuscriptionsByUserIdFallback(userId, e);
        }
    }

    public ResponseEntity<?> getSuscriptionsByUserIdFallback(Long userId, Throwable t) {
        Map<String, Object> fallbackSuscriptions = Map.of("userId", userId, "price", BigDecimal.ZERO);
        return new ResponseEntity<>(fallbackSuscriptions, HttpStatus.OK);
    }

}