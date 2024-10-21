package com.dtaquito_micro_services.rooms_service.rooms.client;

import com.dtaquito_micro_services.rooms_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserClient {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TokenCacheService tokenCacheService;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${predefined.token}")
    private String predefinedToken;

    public ResponseEntity<User> getUserById(Long id, String token) {
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        String url = instances.get(0).getUri().toString() + "/api/v1/users/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println("JSON Response: " + response.getBody());

        return restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
    }

    public ResponseEntity<User> getUserByIdWithPredefinedToken(Long id) {
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        String url = instances.get(0).getUri().toString() + "/api/v1/users/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + predefinedToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println("JSON Response: " + response.getBody());

        return restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
    }

   public void updateCredits(Long id, Integer credits) {
        String token = tokenCacheService.getToken("default");
        if (token == null) {
            throw new IllegalStateException("Token not found");
        }
       List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
       String url = instances.get(0).getUri().toString() + "/api/v1/users/update/credits/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        Map<String, Integer> body = Map.of("credits", credits);

        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (Exception e) {
            // Log the error and rethrow it or handle it as needed
            log.error("Failed to update credits for user: " + id, e);
            throw new RuntimeException("Failed to update credits", e);
        }
    }

    public void deductCredits(Long id, Integer credits) {
        String token = tokenCacheService.getToken("default");
        if (token == null) {
            throw new IllegalStateException("Token not found");
        }
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        String url = instances.get(0).getUri().toString() + "/api/v1/users/deduct/credits/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        Map<String, Integer> body = Map.of("credits", credits);

        HttpEntity<Map<String, Integer>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (Exception e) {
            // Log the error and rethrow it or handle it as needed
            log.error("Failed to update credits for user: " + id, e);
            throw new RuntimeException("Failed to update credits", e);
        }
    }
}