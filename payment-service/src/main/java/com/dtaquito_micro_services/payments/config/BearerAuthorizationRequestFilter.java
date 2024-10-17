package com.dtaquito_micro_services.payments.config;

import com.dtaquito_micro_services.payments.config.impl.TokenCacheService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bearer Authorization Request Filter.
 * <p>
 * This class is responsible for filtering requests and setting the user authentication.
 * It extends the OncePerRequestFilter class.
 * </p>
 * @see OncePerRequestFilter
 */
@Component
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
    private final BearerTokenService tokenService;
    private final TokenCacheService tokenCacheService;

    @Qualifier("defaultUserDetailsService")
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, TokenCacheService tokenCacheService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.tokenCacheService = tokenCacheService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = tokenService.getBearerTokenFrom(request);
            LOGGER.info("Extracted Token: {}", token);
            if (token != null && tokenService.validateToken(token)) {
                Claims claims = tokenService.getClaimsFromToken(token);
                String email = claims.getSubject();
                LOGGER.info("Extracted Email: {}", email);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                LOGGER.info("Loaded UserDetails: {}", userDetails);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                LOGGER.info("Authentication set for user: {}", email);

                // Store the token in the TokenCacheService
                tokenCacheService.storeToken("default", token);
                LOGGER.info("Stored token in TokenCacheService: {}", token);
            } else {
                LOGGER.info("Token is not valid");
            }
        } catch (Exception e) {
            LOGGER.error("Cannot set user authentication: {}", e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }
}