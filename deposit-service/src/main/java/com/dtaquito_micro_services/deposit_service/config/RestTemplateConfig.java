package com.dtaquito_micro_services.deposit_service.config;

import com.dtaquito_micro_services.deposit_service.config.impl.TokenCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

@Configuration
public class RestTemplateConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);
    private final TokenCacheService tokenCacheService;

    public RestTemplateConfig(TokenCacheService tokenCacheService) {
        this.tokenCacheService = tokenCacheService;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.setInterceptors(Collections.singletonList(bearerTokenInterceptor()));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestInterceptor bearerTokenInterceptor() {
        return (request, body, execution) -> {
            String token = tokenCacheService.getToken("default");

            if (token == null) {
                token = UUID.randomUUID().toString();
                logger.debug("Generated new token: {}", token);
                tokenCacheService.storeToken("default", token);
            } else {
                logger.debug("Retrieved token from cache: {}", token);
            }

            request.getHeaders().add("Authorization", "Bearer " + token);
            logger.debug("Added Authorization header with token: {}", token);

            return execution.execute(request, body);
        };
    }
}