package com.dtaquito_micro_services.deposit_service.deposit.interfaces.rest;

import com.dtaquito_micro_services.deposit_service.config.BearerTokenService;
import com.dtaquito_micro_services.deposit_service.config.TokenService;
import com.dtaquito_micro_services.deposit_service.deposit.client.PaymentClient;
import com.dtaquito_micro_services.deposit_service.deposit.client.UserClient;
import com.dtaquito_micro_services.deposit_service.deposit.domain.model.aggregates.User;
import com.dtaquito_micro_services.deposit_service.deposit.domain.model.commands.CreateDepositCommand;
import com.dtaquito_micro_services.deposit_service.deposit.domain.services.DepositCommandService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value="/api/v1/deposit", produces = MediaType.APPLICATION_JSON_VALUE)
public class DepositController {

    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final DepositCommandService commandService;
    private final BearerTokenService bearerTokenService;
    private final TokenService tokenService;

    public DepositController(UserClient userClient, PaymentClient paymentClient, DepositCommandService commandService, BearerTokenService bearerTokenService,
                             TokenService tokenService) {
        this.userClient = userClient;
        this.paymentClient = paymentClient;
        this.commandService = commandService;
        this.bearerTokenService = bearerTokenService;
        this.tokenService = tokenService;
    }

    @PostMapping("/create-deposit")
    @CircuitBreaker(name = "default", fallbackMethod = "createDepositFallback")
    public ResponseEntity<String> createDeposit(@RequestParam BigDecimal amount, HttpServletRequest request) {
        String token = bearerTokenService.getBearerTokenFrom(request);
        if (token == null || !tokenService.validateToken(token)) {
            log.error("Token is not valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is not valid");
        }
        String userId = tokenService.getUserIdFromToken(token);

        ResponseEntity<User> userResponse = userClient.getUserById(Long.parseLong(userId), token);
        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            log.error("User not found: " + userId);
            return createDepositFallback(amount, request, new RuntimeException("User not found"));
        }

        User user = userResponse.getBody();
        log.info("User found: " + user.getEmail() + ", Role: " + user.getRoleType());

        if (user.getRoleType().equals("P")) {
            log.error("User role not authorized: " + user.getRoleType());
            return ResponseEntity.status(403).body("User not authorized");
        }

        try {
            // Call createDepositPayment method from PaymentClient
            ResponseEntity<Map<String, String>> paymentResponse = paymentClient.createDepositPayment(amount);
            if (paymentResponse.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.status(500).body("Failed to generate PayPal payment link");
            }

            String paymentLink = paymentResponse.getBody().get("approval_url");
            log.info("Generated PayPal payment link: " + paymentLink);

            // Return the payment link to the client immediately
            ResponseEntity<String> responseEntity = ResponseEntity.ok(Map.of("paymentLink", paymentLink).toString());

            // Start a new thread to check the payment status after a delay
            new Thread(() -> {
                try {
                    // Simulate waiting for payment processing (e.g., sleep for a few seconds)
                    Thread.sleep(20000); // Adjust the sleep time as needed

                    // Check payment status using userId
                    boolean paymentApproved = paymentClient.checkPaymentStatus(userId);

                    if (paymentApproved) {
                        CreateDepositCommand command = new CreateDepositCommand(user.getId(), amount.longValue());
                        commandService.handle(command);
                        //user.setCredits(user.getCredits().add(amount));
                        userClient.updateCredits(user.getId(), amount);
                        log.info("Payment approved and deposit created for user: " + userId);
                    } else {
                        log.warn("Payment not approved for user: " + userId);
                    }
                } catch (Exception e) {
                    log.error("Error occurred while checking payment status", e);
                }
            }).start();

            return responseEntity;
        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error occurred after generating PayPal payment link", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the deposit");
        }
    }

    public ResponseEntity<String> createDepositFallback(@RequestParam BigDecimal amount, HttpServletRequest request, Throwable t) {
        log.error("Fallback triggered for createDeposit: ", t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Payment service unavailable");
    }
}