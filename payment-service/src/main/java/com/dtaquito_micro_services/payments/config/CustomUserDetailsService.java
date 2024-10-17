package com.dtaquito_micro_services.payments.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final RestTemplate restTemplate;
    private final BearerTokenService tokenService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private DiscoveryClient discoveryClient;

    public CustomUserDetailsService(RestTemplate restTemplate, BearerTokenService tokenService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    @Override
    @CircuitBreaker(name = "default", fallbackMethod = "loadUserByUsernameFallback")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");

        String url = instances.get(0).getUri().toString() +"/api/v1/users?email=" + username;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenService.getBearerTokenFrom(request));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<CustomUserDetails> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, CustomUserDetails.class);
            CustomUserDetails userDetails = response.getBody();
            if (userDetails == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            // Set authorities (this should be retrieved from your user service or database)
            userDetails.setAuthorities(getAuthoritiesForUser(username));

            return userDetails;
        } catch (HttpClientErrorException.Unauthorized e) {
            System.err.println("Unauthorized error: " + e.getMessage());
            throw new UsernameNotFoundException("Unauthorized: Unable to authenticate user", e);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error: " + e.getMessage());
            throw new UsernameNotFoundException("HTTP error: Unable to authenticate user", e);
        }
    }

    public UserDetails loadUserByUsernameFallback(String username, Throwable t) {
        System.err.println("Fallback triggered for user: " + username + " due to: " + t.getMessage());
        // Return a default user with non-null and non-empty values
        return new org.springframework.security.core.userdetails.User(
                "defaultUser", "defaultPassword", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private Collection<? extends GrantedAuthority> getAuthoritiesForUser(String username) {
        // Implement this method to retrieve the authorities for the user
        // For example, you can fetch it from your database or another service
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}