package com.dtaquito_micro_services.user_service.iam.infrastructure.authorization.sfs.pipeline;

import com.dtaquito_micro_services.user_service.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.dtaquito_micro_services.user_service.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.dtaquito_micro_services.user_service.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.netflix.discovery.converters.Auto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import com.dtaquito_micro_services.user_service.config.TokenCacheService;

import java.io.IOException;

/**
 * Bearer Authorization Request Filter.
 * <p>
 * This class is responsible for filtering requests and setting the user authentication.
 * It extends the OncePerRequestFilter class.
 * </p>
 * @see OncePerRequestFilter
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);
    private final BearerTokenService tokenService;

    @Autowired
    private TokenCacheService tokenCacheService;


    @Qualifier("defaultUserDetailsService")
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
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
                String email = tokenService.getEmailFromToken(token);
                LOGGER.info("Extracted Email: {}", email);
                UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
                userDetails.setToken(token);
                LOGGER.info("Loaded UserDetails: {}", userDetails);
                SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request));
                LOGGER.info("Authentication set for user: {}", email);

                tokenCacheService.storeToken("default", token);
            } else {
                LOGGER.info("Token is not valid");
            }
        } catch (Exception e) {
            LOGGER.error("Cannot set user authentication: {}", e.getMessage(), e);
        }
        filterChain.doFilter(request, response);
    }
}