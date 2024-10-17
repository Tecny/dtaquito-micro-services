package com.dtaquito_micro_services.rooms_service.rooms.client;

import com.dtaquito_micro_services.rooms_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.SportSpaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class SportSpaceClient {

    private static final Logger logger = LoggerFactory.getLogger(SportSpaceClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenCacheService tokenCacheService;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${predefined.token}")
    private String predefinedToken;

    public ResponseEntity<SportSpaces> getSportSpaceById(Long id) {
        String token = tokenCacheService.getToken("default");
        if (token == null) {
            throw new IllegalStateException("Token not found");
        }
        List<ServiceInstance> instances = discoveryClient.getInstances("sport-space-service");
        String url = instances.get(0).getUri().toString() + "/api/v1/sports/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<SportSpaces>() {});
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching sport space with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    public ResponseEntity<SportSpaces> getSportSpaceByIdWithPredefinedToken(Long id) {
        List<ServiceInstance> instances = discoveryClient.getInstances("sport-space-service");
        String url = instances.get(0).getUri().toString() + "/api/v1/sports/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + predefinedToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<SportSpaces>() {});
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching sport space with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }
}