package com.dtaquito_backend.dtaquito_backend.payments.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.dtaquito_backend.dtaquito_backend.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.dtaquito_backend.dtaquito_backend.payments.application.internal.commandservices.PayPalPaymentServiceImpl;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentsController {

    private final PayPalPaymentServiceImpl payPalPaymentService;
    private final TokenService tokenService;
    private final BearerTokenService bearerTokenService;


    @PostMapping("/create-deposit-payment")
    public ResponseEntity<Map<String, String>> createDepositPayment(@RequestParam BigDecimal amount, HttpServletRequest request) {
        log.info("Iniciando la creación del pago de depósito por el monto: " + amount);
        try {
            String token = bearerTokenService.getBearerTokenFrom(request);
            if (token == null || !tokenService.validateToken(token)) {
                log.error("Token is not valid");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is not valid"));
            }
            String userId = tokenService.getUserIdFromToken(token);

            // Set amount in session
            request.getSession().setAttribute("amount", amount);

            // Crear el pago con PayPal
            String cancelUrl = "https://dtaquito-backend.azurewebsites.net/api/v1/deposit/payment-deposits/cancel";
            String successUrl = "https://dtaquito-backend.azurewebsites.net/api/v1/deposit/payment-deposits/success?jwtToken=" + token;
            Payment payment = payPalPaymentService.createPayment(
                    amount.doubleValue(), "USD", "paypal", "sale", userId, "Depósito de dinero", cancelUrl, successUrl);

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    Map<String, String> responseMap = Map.of("approval_url", links.getHref());
                    return ResponseEntity.ok(responseMap);
                }
            }

        } catch (PayPalRESTException e) {
            log.error("Error al crear el pago de depósito", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al crear el pago de depósito"));
    }
}