package com.dtaquito_micro_services.user_service.iam.infrastructure.tokens.jwt.services;

import com.dtaquito_micro_services.user_service.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class TokenServiceImpl implements BearerTokenService {
    private final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);
    private final SecretKey signingKey;

    public TokenServiceImpl(SecretKey signingKey) {
        this.signingKey = signingKey;
    }

    private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_BEGIN_INDEX = 7;

    @Value("${authorization.jwt.expiration.days}")
    private int expirationDays;

    @Override
    public String generateToken(Authentication authentication) {
        LOGGER.debug("Generating token for authentication: {}", authentication);
        String token = buildTokenWithDefaultParameters(authentication.getPrincipal().toString(), authentication.getName());
        LOGGER.debug("Generated token: {}", token);
        LOGGER.info("Complete JWT: {}", token);
        return token;
    }

    public String generateToken(String username, String userId) {
        LOGGER.debug("Generating token for username: {}", username);
        String token = buildTokenWithDefaultParameters(username, userId);
        LOGGER.debug("Generated token: {}", token);
        return token;
    }

    private String buildTokenWithDefaultParameters(String username, String userId) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // Add userId to claims
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", String.class);
    }

    @Override
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        LOGGER.debug("Validating token: {}", token);
        try {
            Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
            LOGGER.info("Token is valid");
            return true;
        } catch (SignatureException e) {
            LOGGER.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenPresentIn(String authorizationParameter) {
        return StringUtils.hasText(authorizationParameter);
    }

    private boolean isBearerTokenIn(String authorizationParameter) {
        return authorizationParameter.startsWith(BEARER_TOKEN_PREFIX);
    }

    private String extractTokenFrom(String authorizationHeaderParameter) {
        return authorizationHeaderParameter.substring(TOKEN_BEGIN_INDEX);
    }

    private String getAuthorizationParameterFrom(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_PARAMETER_NAME);
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            LOGGER.info("Received JWT: {}", token);
            return token;
        }
        return null;
    }
}