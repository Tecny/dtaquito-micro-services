// DepositController.java
package com.dtaquito_backend.dtaquito_backend.deposit.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_backend.dtaquito_backend.deposit.infrastructure.persistance.jpa.DepositRepository;
import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.dtaquito_backend.dtaquito_backend.payments.application.internal.commandservices.PayPalPaymentServiceImpl;
import com.dtaquito_backend.dtaquito_backend.payments.domain.model.aggregates.Payments;
import com.dtaquito_backend.dtaquito_backend.payments.infrastructure.persistance.jpa.PaymentsRepository;
import com.dtaquito_backend.dtaquito_backend.payments.interfaces.rest.PaymentsController;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.paypal.api.payments.Payment;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value="/api/v1/deposit", produces = MediaType.APPLICATION_JSON_VALUE)
public class DepositController {

    private final UserRepository userRepository;
    private final PayPalPaymentServiceImpl payPalPaymentService;
    private final DepositRepository depositRepository;
    private final PaymentsController paymentController;
    private final BearerTokenService bearerTokenService;
    private final TokenService tokenService;
    private final PaymentsRepository paymentsRepository;

    public DepositController(UserRepository userRepository, PayPalPaymentServiceImpl payPalPaymentService, DepositRepository depositRepository,
                             PaymentsController paymentController, BearerTokenService bearerTokenService,
                             TokenService tokenService, PaymentsRepository paymentsRepository) {
        this.userRepository = userRepository;
        this.payPalPaymentService = payPalPaymentService;
        this.depositRepository = depositRepository;
        this.paymentController = paymentController;
        this.bearerTokenService = bearerTokenService;
        this.tokenService = tokenService;
        this.paymentsRepository = paymentsRepository;
    }

    // DepositController.java
    @PostMapping("/create-deposit")
    public ResponseEntity<String> createDeposit(@RequestParam BigDecimal amount, HttpServletRequest request) {
        String token = bearerTokenService.getBearerTokenFrom(request);
        if (token == null || !tokenService.validateToken(token)) {
            log.error("Token is not valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is not valid");
        }
        String userId = tokenService.getUserIdFromToken(token);

        Optional<User> userOptional = userRepository.findById(Long.parseLong(userId));
        if (userOptional.isEmpty()) {
            log.error("User not found: " + userId);
            return ResponseEntity.status(403).body("User not authorized");
        }

        User user = userOptional.get();
        log.info("User found: " + user.getEmail() + ", Role: " + user.getRole().getRoleType());

        // Allow only users with role "R" to create a deposit
        if (user.getRole().getRoleType().equals("P")) {
            log.error("User role not authorized: " + user.getRole().getRoleType());
            return ResponseEntity.status(403).body("User not authorized");
        }

        // Call createDepositPayment method from PaymentsController
        ResponseEntity<Map<String, String>> paymentResponse = paymentController.createDepositPayment(amount, request);
        if (paymentResponse.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(500).body("Failed to generate PayPal payment link");
        }

        String paymentLink = paymentResponse.getBody().get("approval_url");
        log.info("Generated PayPal payment link: " + paymentLink);

        return ResponseEntity.ok(paymentLink);
    }

    @GetMapping("/payment-deposits/success")
    public ResponseEntity<Map<String, String>> paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId, @RequestParam("jwtToken") String jwtToken, HttpServletRequest request) {
        log.info("Payment success callback received with paymentId: {} and PayerID: {}", paymentId, payerId);
        try {
            if (jwtToken == null || !tokenService.validateToken(jwtToken)) {
                log.error("Token is not valid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is not valid"));
            }
            String userId = tokenService.getUserIdFromToken(jwtToken);

            Payment payment = payPalPaymentService.executePayment(paymentId, payerId);
            log.debug("Payment details: {}", payment);

            if (payment.getState().equals("approved")) {
                Optional<User> userOptional = userRepository.findById(Long.parseLong(userId));
                if (userOptional.isEmpty()) {
                    log.error("User not found: " + userId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
                }

                User user = userOptional.get();
                BigDecimal amount = new BigDecimal(payment.getTransactions().get(0).getAmount().getTotal());

                Deposit deposit = new Deposit();
                deposit.setUser(user);
                deposit.setAmount(amount);
                depositRepository.save(deposit);

                user.setCredits(user.getCredits().add(amount));
                userRepository.save(user);

                Payments transaction = new Payments();
                transaction.setTransactionId(paymentId);
                transaction.setUserId(userId);
                transaction.setPaymentStatus("APPROVED");
                transaction.setAmount(amount);
                transaction.setCurrency(payment.getTransactions().get(0).getAmount().getCurrency());
                paymentsRepository.save(transaction);

                return ResponseEntity.ok(Map.of("status", "paymentSuccess"));
            } else {
                log.error("Payment not approved: " + payment.getState());
                payPalPaymentService.refundPayment(paymentId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Payment not approved"));
            }
        } catch (Exception e) {
            log.error("Error processing payment success callback", e);
            payPalPaymentService.refundPayment(paymentId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error processing payment", "message", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Deposit>> getAllDeposits() {
        List<Deposit> deposits = depositRepository.findAll();
        return ResponseEntity.ok(deposits);
    }
}