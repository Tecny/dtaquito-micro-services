package com.dtaquito_micro_services.sport_service.sportspaces.client;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class UserClient {

    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);

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

        logger.info("Requesting user with ID: {}", id);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        logger.info("JSON Response: {}", response.getBody());

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
        } catch (ResourceAccessException e) {
            logger.error("ResourceAccessException: {}", e.getMessage());
            throw e;
        } catch (HttpClientErrorException e) {
            logger.error("HttpClientErrorException: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}