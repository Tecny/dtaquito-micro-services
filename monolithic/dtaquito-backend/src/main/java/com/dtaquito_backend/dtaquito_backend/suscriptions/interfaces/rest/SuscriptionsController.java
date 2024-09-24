package com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dtaquito_backend.dtaquito_backend.payments.application.internal.commandservices.PayPalPaymentServiceImpl;
import com.dtaquito_backend.dtaquito_backend.payments.domain.model.aggregates.Payments;
import com.dtaquito_backend.dtaquito_backend.payments.infrastructure.persistance.jpa.PaymentsRepository;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.suscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.resources.CreateSuscriptionsResource;
import com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.transform.CreateSuscriptionsCommandFromResourceAssembler;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserCommandService;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.queries.GetAllSuscriptionsByPlanQuery;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.queries.GetSuscriptionsByIdQuery;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsQueryService;
import com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.resources.SuscriptionsResource;
import com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.transform.SuscriptionsResourceFromEntityAssembler;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/suscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
public class SuscriptionsController {

    private final SuscriptionsQueryService suscriptionsQueryService;
    private final SuscriptionsCommandService suscriptionsCommandService;
    private final UserCommandService userCommandService;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final PayPalPaymentServiceImpl payPalPaymentService;
    private final PaymentsRepository paymentsRepository;

    public SuscriptionsController(SuscriptionsQueryService suscriptionsQueryService, SuscriptionsCommandService suscriptionsCommandService, UserCommandService userCommandService, PlanRepository planRepository
            , UserRepository userRepository, PayPalPaymentServiceImpl payPalPaymentService, PaymentsRepository paymentsRepository) {
        this.suscriptionsQueryService = suscriptionsQueryService;
        this.suscriptionsCommandService = suscriptionsCommandService;
        this.userCommandService = userCommandService;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
        this.payPalPaymentService = payPalPaymentService;
        this.paymentsRepository = paymentsRepository;
    }

    @PostMapping("/select")
    public ResponseEntity<Void> selectSubscription(@RequestParam Long userId, @RequestParam PlanTypes planType) {
        try {
            Plan plan = planRepository.findByPlanType(planType)
                    .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
            CreateSuscriptionsCommand command = new CreateSuscriptionsCommand(plan.getId(), userId, null);
            suscriptionsCommandService.handle(command);

            // Redirect to payment page
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .header(HttpHeaders.LOCATION, "/payment/initialize?userId=" + userId + "&planType=" + planType)
                    .build();
        } catch (Exception e) {
            System.out.println("Error selecting subscription: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public void updateSubscription(Long userId, String newPlan) {
        var user = userCommandService.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPlan(newPlan);
        userCommandService.save(user);
    }

    @PutMapping("/upgrade")
    public ResponseEntity<String> upgradeSubscription(@RequestParam Long userId, @RequestParam PlanTypes newPlanType) {
        try {
            // Retrieve the user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Validate the upgrade
            PlanTypes currentPlanType = PlanTypes.valueOf(user.getPlan());
            if (currentPlanType == PlanTypes.oro) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is already at the highest plan level");
            }
            if ((currentPlanType == PlanTypes.bronce && newPlanType != PlanTypes.plata && newPlanType != PlanTypes.oro) ||
                    (currentPlanType == PlanTypes.plata && newPlanType != PlanTypes.oro)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid upgrade path");
            }

            // Set payment amount based on new plan type
            BigDecimal amount;
            if (newPlanType.equals(PlanTypes.oro)) {
                amount = new BigDecimal("14.99");
            } else if (newPlanType.equals(PlanTypes.plata)) {
                amount = new BigDecimal("9.99");
            } else if (newPlanType.equals(PlanTypes.bronce)) {
                amount = new BigDecimal("4.99");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid plan type");
            }

            // Process payment with PayPal
            String cancelUrl = "https://dtaquito-backend.azurewebsites.net/api/v1/suscriptions/upgrade/cancel";
            String successUrl = "https://dtaquito-backend.azurewebsites.net/api/v1/suscriptions/upgrade/success";
            Payment payment = payPalPaymentService.createPayment(
                    amount.doubleValue(), "USD", "paypal", "sale", userId.toString(), "Subscription upgrade", cancelUrl, successUrl);

            // Save payment details
            Payments transaction = new Payments();
            transaction.setTransactionId(payment.getId());
            transaction.setUserId(userId.toString());
            transaction.setPaymentStatus("PENDING");
            transaction.setAmount(amount);
            transaction.setCurrency("USD");
            paymentsRepository.save(transaction);


            for (com.paypal.api.payments.Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String json = objectMapper.writeValueAsString(Map.of("approval_url", links.getHref()));
                    return ResponseEntity.ok(json);
                }
            }

            // Additional logic from updateAllowedSportSpacesBySuscription
            Plan plan = planRepository.findByPlanType(newPlanType)
                    .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

            if (PlanTypes.free.equals(currentPlanType) &&
                    (PlanTypes.bronce.equals(plan.getPlanType()) || PlanTypes.plata.equals(plan.getPlanType()) || PlanTypes.oro.equals(plan.getPlanType()))) {
                // Redirect to payment creation endpoint
                return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                        .header(HttpHeaders.LOCATION, "/api/v1/payments/create?userId=" + userId + "&planType=" + plan.getPlanType().name())
                        .build();
            }

            // Update allowedSportspaces based on planType
            int allowedSportspaces;
            switch (plan.getPlanType()) {
                case bronce:
                    allowedSportspaces = 1;
                    break;
                case plata:
                    allowedSportspaces = 2;
                    break;
                case oro:
                    allowedSportspaces = 3;
                    break;
                case free:
                default:
                    allowedSportspaces = 0;
                    break;
            }
            user.updateAllowedSportspaces(allowedSportspaces);

            updateSubscription(user.getId(), plan.getPlanType().name());

            return ResponseEntity.ok("Subscription upgraded successfully");
        } catch (PayPalRESTException e) {
            log.error("Error processing PayPal payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing payment");
        } catch (Exception e) {
            log.error("Error upgrading subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error upgrading subscription");
        }
    }

    @GetMapping("/upgrade/success")
    public String upgradeSuccess(@RequestParam("paymentId") String paymentId,
                                 @RequestParam("PayerID") String payerId) {
        log.info("Upgrade success callback received with paymentId: {} and PayerID: {}", paymentId, payerId);
        try {
            Payment payment = payPalPaymentService.executePayment(paymentId, payerId);
            log.debug("Payment details: {}", payment);

            if (payment.getState().equals("approved")) {
                // Retrieve the transaction
                Payments transaction = paymentsRepository.findByTransactionId(paymentId)
                        .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

                // Retrieve the user
                User user = userRepository.findById(Long.valueOf(transaction.getUserId()))
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                // Update the user's plan
                PlanTypes newPlanType;
                BigDecimal amount = transaction.getAmount();
                if (amount.equals(new BigDecimal("14.99"))) {
                    newPlanType = PlanTypes.oro;
                } else if (amount.equals(new BigDecimal("9.99"))) {
                    newPlanType = PlanTypes.plata;
                } else if (amount.equals(new BigDecimal("4.99"))) {
                    newPlanType = PlanTypes.bronce;
                } else {
                    newPlanType = PlanTypes.free;
                }
                user.setPlan(newPlanType.name());
                user.setAllowedSportspacesBasedOnPlan();
                userRepository.save(user);

                // Update the existing subscription in the suscriptions table
                Suscriptions existingSubscription = suscriptionsQueryService.getSubscriptionByUserId(user.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
                Plan selectedPlan = planRepository.findByPlanType(newPlanType)
                        .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
                existingSubscription.update(selectedPlan);
                suscriptionsCommandService.updateSuscription(existingSubscription);

                // Update transaction status
                transaction.setPaymentStatus("APPROVED");
                paymentsRepository.save(transaction);

                log.info("Upgrade approved and userId {} set in Payments table", user.getId());
                return "upgradeSuccess";
            } else {
                log.error("Payment state is not approved: {}", payment.getState());
                return "upgradeError";
            }
        } catch (Exception e) {
            log.error("Error processing upgrade success callback", e);
            return "upgradeError";
        }
    }

    @GetMapping("/upgrade/cancel")
    public String upgradeCancel(@RequestParam("paymentId") String paymentId) {
        log.info("Upgrade cancelled with paymentId: {}", paymentId);

        // Save transaction data with status "CANCELLED"
        Payments transaction = new Payments();
        transaction.setTransactionId(paymentId);
        transaction.setPaymentStatus("CANCELLED");
        paymentsRepository.save(transaction);

        return "upgradeCancel";
    }

    @GetMapping()
    public ResponseEntity<?> getSubscriptionByUserId(@RequestParam Long userId) {
        Optional<Suscriptions> subscription = suscriptionsQueryService.getSubscriptionByUserId(userId);
        if (subscription.isPresent()) {
            return ResponseEntity.ok(SuscriptionsResourceFromEntityAssembler.toResourceFromEntity(subscription.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

