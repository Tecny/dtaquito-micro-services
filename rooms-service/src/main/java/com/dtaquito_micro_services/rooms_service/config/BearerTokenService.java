package com.dtaquito_micro_services.rooms_service.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface BearerTokenService extends TokenService {

    String getBearerTokenFrom(HttpServletRequest token);

    String generateToken(Authentication authentication);
}
