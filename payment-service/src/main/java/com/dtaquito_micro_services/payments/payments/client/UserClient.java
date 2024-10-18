package com.dtaquito_micro_services.payments.payments.client;

import com.dtaquito_micro_services.payments.payments.domain.model.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class UserClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

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
}