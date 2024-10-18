package com.dtaquito_micro_services.payments.payments.interfaces.rest;

import com.dtaquito_micro_services.payments.config.CustomUserDetails;
import com.dtaquito_micro_services.payments.config.impl.TokenCacheService;
import com.dtaquito_micro_services.payments.payments.application.internal.commandservices.PayPalPaymentServiceImpl;
import com.dtaquito_micro_services.payments.config.BearerTokenService;
import com.dtaquito_micro_services.payments.payments.application.internal.commandservices.PaymentTimestampService;
import com.dtaquito_micro_services.payments.payments.client.SuscriptionClient;
import com.dtaquito_micro_services.payments.payments.client.UserClient;
import com.dtaquito_micro_services.payments.payments.domain.model.aggregates.Payments;
import com.dtaquito_micro_services.payments.payments.domain.model.entities.Plan;
import com.dtaquito_micro_services.payments.payments.domain.model.entities.Suscriptions;
import com.dtaquito_micro_services.payments.payments.domain.model.entities.User;
import com.dtaquito_micro_services.payments.payments.domain.model.valueobjects.PlanTypes;
import com.dtaquito_micro_services.payments.payments.infrastructure.persistance.jpa.PaymentsRepository;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentsController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsController.class);

    private final PayPalPaymentServiceImpl payPalPaymentService;
    private final BearerTokenService tokenService;
    private final SuscriptionClient suscriptionClient;
    private final PaymentsRepository paymentsRepository;
    private final UserClient userClient;
    private final TokenCacheService tokenCacheService;

    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private PaymentTimestampService paymentTimestampService;

    public PaymentsController(PayPalPaymentServiceImpl payPalPaymentService, BearerTokenService tokenService, SuscriptionClient suscriptionClient, PaymentsRepository paymentsRepository, UserClient userClient, TokenCacheService tokenCacheService) {
        this.payPalPaymentService = payPalPaymentService;
        this.tokenService = tokenService;
        this.suscriptionClient = suscriptionClient;
        this.paymentsRepository = paymentsRepository;
        this.userClient = userClient;
        this.tokenCacheService = tokenCacheService;
    }

    @PostMapping("/create-deposit-payment")
    public ResponseEntity<Map<String, String>> createDepositPayment(@RequestParam BigDecimal amount, HttpServletRequest request) {

        // Log the request details
        logger.debug("Received request to create deposit payment with amount: {}", amount);

        String token = tokenService.getBearerTokenFrom(request);
        if (token == null || !tokenService.validateToken(token)) {
            logger.error("Token is not valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is not valid"));
        }

        Claims claims = tokenService.getClaimsFromToken(token);
        String userId = claims.get("userId", String.class);
        logger.debug("User ID from token: {}", userId);

        List<ServiceInstance> instances = discoveryClient.getInstances("payment-service");
        String cancelUrl = instances.get(0).getUri().toString() + "/api/v1/payments/create/cancel";
        String successUrl = instances.get(0).getUri().toString() + "/api/v1/payments/create/success";
        try {
            Payment payment = payPalPaymentService.createPayment(
                    amount.doubleValue(), "USD", "paypal", "sale", userId, "Depósito de dinero", cancelUrl, successUrl);

            Payments transaction = new Payments();
            transaction.setTransactionId(payment.getId());
            transaction.setUserId(userId);
            transaction.setPaymentStatus("PENDING");
            transaction.setAmount(amount);
            transaction.setCurrency("USD");
            paymentsRepository.save(transaction);

            // Store the token in the cache
            tokenCacheService.storeToken(payment.getId(), token);
            paymentTimestampService.storeTimestamp(payment.getId());


            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    logger.debug("Approval URL: {}", links.getHref());
                    logger.debug("Payment created successfully, approval URL: {}", links.getHref());
                    return ResponseEntity.ok(Map.of("approval_url", links.getHref()));
                }
            }
        } catch (PayPalRESTException e) {
            logger.error("Error creating deposit payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creating deposit payment"));
        }
        logger.error("Error creating deposit payment");
        return createDepositPaymentFallback(amount, request, new Exception("Error creating deposit payment"));
    }

    public ResponseEntity<Map<String, String>> createDepositPaymentFallback(BigDecimal amount, HttpServletRequest request, Throwable t) {

        logger.error("Error creating deposit payment", t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Service is currently unavailable. Please try again later."));
    }

    @PostMapping("/create-subscription-payment")
    public ResponseEntity<Map<String, String>> createSubscriptionPayment(@RequestParam BigDecimal amount, @RequestParam String planType, HttpServletRequest request) {
        logger.debug("Received request to create subscription payment with amount: {} and planType: {}", amount, planType);

        String token = tokenService.getBearerTokenFrom(request);
        if (token == null || !tokenService.validateToken(token)) {
            logger.error("Token is not valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is not valid"));
        }

        Claims claims = tokenService.getClaimsFromToken(token);
        String userId = claims.get("userId", String.class);
        logger.debug("User ID from token: {}", userId);

        List<ServiceInstance> instances = discoveryClient.getInstances("payment-service");
        String cancelUrl = instances.get(0).getUri().toString() + "/api/v1/payments/upgrade/cancel";
        String successUrl = instances.get(0).getUri().toString() + "/api/v1/payments/upgrade/success";
        try {
            Payment payment = payPalPaymentService.createPayment(
                    amount.doubleValue(), "USD", "paypal", "sale", userId, "Subscription payment for plan: " + planType, cancelUrl, successUrl);

            Payments transaction = new Payments();
            transaction.setTransactionId(payment.getId());
            transaction.setUserId(userId);
            transaction.setPaymentStatus("PENDING");
            transaction.setAmount(amount);
            transaction.setCurrency("USD");
            paymentsRepository.save(transaction);

            // Store the token in the cache
            tokenCacheService.storeToken(payment.getId(), token);
            paymentTimestampService.storeTimestamp(payment.getId());


            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    logger.debug("Approval URL: {}", links.getHref());
                    return ResponseEntity.ok(Map.of("approval_url", links.getHref()));
                }
            }
        } catch (PayPalRESTException e) {
            logger.error("Error creating subscription payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creating subscription payment"));
        }
        logger.error("Error creating subscription payment");
        return createSubscriptionPaymentFallback(amount, planType, request, new Exception("Error creating subscription payment"));
    }

    public ResponseEntity<Map<String, String>> createSubscriptionPaymentFallback(BigDecimal amount, String planType, HttpServletRequest request, Throwable t) {
        logger.error("Error creating subscription payment", t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/upgrade/success")
    public String upgradeSuccess(@RequestParam("paymentId") String paymentId,
                                 @RequestParam("PayerID") String payerId) {
        log.info("Upgrade success callback received with paymentId: {} and PayerID: {}", paymentId, payerId);
        try {
            Payment payment = payPalPaymentService.executePayment(paymentId, payerId);
            log.debug("Payment details: {}", payment);

            if (payment.getState().equals("approved")) {
                Payments transaction = paymentsRepository.findByTransactionId(paymentId)
                        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

                // Check if the payment is within the 20-second window
                LocalDateTime timestamp = paymentTimestampService.getTimestamp(paymentId);
                if (timestamp == null || timestamp.isBefore(LocalDateTime.now().minusSeconds(20))) {
                    log.error("Payment link has expired");
                    payPalPaymentService.refundPayment(paymentId);
                    return "upgradeError";
                }

                // Retrieve the token from the cache
                String token = tokenCacheService.getToken(paymentId);
                if (token == null || !tokenService.validateToken(token)) {
                    log.error("Token is not valid");
                    return "upgradeError";
                }

                ResponseEntity<User> userResponse = userClient.getUserById(Long.valueOf(transaction.getUserId()), token);
                User user = userResponse.getBody();
                if (user == null) {
                    throw new IllegalArgumentException("User not found");
                }

                transaction.setPaymentStatus("APPROVED");
                paymentsRepository.save(transaction);

                // Remove the token from the cache
                tokenCacheService.removeToken(paymentId);
                paymentTimestampService.removeTimestamp(paymentId);

                log.info("Upgrade approved and userId {} set in Payments table", user.getId());
                return "upgradeSuccess";
            } else {
                log.error("Payment state is not approved: {}", payment.getState());
                payPalPaymentService.refundPayment(paymentId);
                return "upgradeError";
            }
        } catch (Exception e) {
            log.error("Error processing upgrade success callback", e);
            payPalPaymentService.refundPayment(paymentId);
            return "upgradeError";
        }
    }

    @GetMapping("/create/cancel")
    public String createCancel() {
        log.info("Create payment cancel callback received");
        return "createCancel";
    }


    @GetMapping("/create/success")
    public String createSuccess(@RequestParam("paymentId") String paymentId,
                                 @RequestParam("PayerID") String payerId) {
        log.info("Upgrade success callback received with paymentId: {} and PayerID: {}", paymentId, payerId);
        try {
            Payment payment = payPalPaymentService.executePayment(paymentId, payerId);
            log.debug("Payment details: {}", payment);

            if (payment.getState().equals("approved")) {
                Payments transaction = paymentsRepository.findByTransactionId(paymentId)
                        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

                // Check if the payment is within the 20-second window
                LocalDateTime timestamp = paymentTimestampService.getTimestamp(paymentId);
                if (timestamp == null || timestamp.isBefore(LocalDateTime.now().minusSeconds(20))) {
                    log.error("Payment link has expired");
                    payPalPaymentService.refundPayment(paymentId);
                    return "createError";
                }

                // Retrieve the token from the cache
                String token = tokenCacheService.getToken(paymentId);
                if (token == null || !tokenService.validateToken(token)) {
                    log.error("Token is not valid");
                    return "createError";
                }

                ResponseEntity<User> userResponse = userClient.getUserById(Long.valueOf(transaction.getUserId()), token);
                User user = userResponse.getBody();
                if (user == null) {
                    throw new IllegalArgumentException("User not found");
                }

                transaction.setPaymentStatus("APPROVED");
                paymentsRepository.save(transaction);

                // Remove the token from the cache
                tokenCacheService.removeToken(paymentId);
                paymentTimestampService.removeTimestamp(paymentId);

                log.info("Upgrade approved and userId {} set in Payments table", user.getId());
                return "createSuccess";
            } else {
                log.error("Payment state is not approved: {}", payment.getState());
                payPalPaymentService.refundPayment(paymentId);
                return "createError";
            }
        } catch (Exception e) {
            log.error("Error processing upgrade success callback", e);
            payPalPaymentService.refundPayment(paymentId);
            return "createError";
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getPaymentStatus(@RequestParam("userId") String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User ID is required"));
        }

        List<Payments> payments = paymentsRepository.findLatestPaymentByUserId(userId);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "No payments found for user"));
        }

        Payments latestPayment = payments.get(0);
        return ResponseEntity.ok(Map.of("status", latestPayment.getPaymentStatus()));
    }

    @GetMapping("/suscriptions/{userId}")
    public ResponseEntity<?> getSuscriptionsByUserId(@PathVariable Long userId) {
        return suscriptionClient.getSuscriptionsByUserId(userId);
    }

    @PostMapping("/suscriptions/upgrade")
    public ResponseEntity<String> upgradeSubscription(@RequestParam Long userId, @RequestParam PlanTypes newPlanType) {
        try{
            return suscriptionClient.upgradeSubscription(userId, newPlanType);
        } catch (ResourceAccessException e) {
            log.error("Error upgrading subscription", e);
            return upgradeSubscriptionFallback(userId, newPlanType, e);
        }
    }

    public ResponseEntity<String> upgradeSubscriptionFallback(Long userId, PlanTypes newPlanType, Throwable t) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service is currently unavailable. Please try again later.");
    }
}