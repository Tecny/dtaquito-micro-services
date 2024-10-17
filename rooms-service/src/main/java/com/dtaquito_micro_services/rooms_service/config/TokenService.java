package com.dtaquito_micro_services.rooms_service.config;

import io.jsonwebtoken.Claims;

public interface TokenService {

    /**
     * Generate a token for a given username
     * @return String the token
     */
    String generateToken(String email, String userId);

    /**
     * Extract the username from a token
     * @param token the token
     * @return String the username
     */
    Claims getClaimsFromToken(String token);
    String getEmailFromToken(String token);

    String getUserIdFromToken(String token);

    /**
     * Validate a token
     * @param token the token
     * @return boolean true if the token is valid, false otherwise
     */
    boolean validateToken(String token);


}