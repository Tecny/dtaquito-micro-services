package com.dtaquito_micro_services.subscription_service.suscriptions.interfaces.rest;

import brave.Response;
import com.dtaquito_micro_services.subscription_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.subscription_service.suscriptions.client.PaymentClient;
import com.dtaquito_micro_services.subscription_service.suscriptions.client.UserClient;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities.Plan;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities.User;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.queries.GetAllSuscriptionsByPlanQuery;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.queries.GetSuscriptionsByIdQuery;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.services.SuscriptionsCommandService;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.services.SuscriptionsQueryService;
import com.dtaquito_micro_services.subscription_service.suscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_micro_services.subscription_service.suscriptions.interfaces.rest.resources.SuscriptionsResource;
import com.dtaquito_micro_services.subscription_service.suscriptions.interfaces.rest.transform.SuscriptionsResourceFromEntityAssembler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/suscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
public class SuscriptionsController {

    private final SuscriptionsQueryService suscriptionsQueryService;
    private final SuscriptionsCommandService suscriptionsCommandService;
    private final PlanRepository planRepository;
    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final TokenCacheService tokenCacheService;

    public SuscriptionsController(SuscriptionsQueryService suscriptionsQueryService, SuscriptionsCommandService suscriptionsCommandService, PlanRepository planRepository, UserClient userClient, PaymentClient paymentClient, TokenCacheService tokenCacheService) {
        this.suscriptionsQueryService = suscriptionsQueryService;
        this.suscriptionsCommandService = suscriptionsCommandService;
        this.planRepository = planRepository;
        this.userClient = userClient;
        this.paymentClient = paymentClient;
        this.tokenCacheService = tokenCacheService;
    }

    @GetMapping("{id}")
    @CircuitBreaker(name = "default", fallbackMethod = "getSubscriptionByIdFallback")
    public ResponseEntity<SuscriptionsResource> getSuscripcionById(@PathVariable Long id) {
        log.info("Fetching subscription by ID: {}", id);
        Optional<Suscriptions> suscriptions = suscriptionsQueryService.handle(new GetSuscriptionsByIdQuery(id));
        if (suscriptions.isEmpty()) {
            log.warn("Subscription not found for ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        Suscriptions suscription = suscriptions.get();
        Long userId = suscription.getUserId();
        String token = tokenCacheService.getToken("default");
        ResponseEntity<User> userResponseEntity;
        try {
            userResponseEntity = userClient.getUserById(userId, token);
        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException for user ID: {}", userId, e);
            return getSubscriptionByIdFallback(id, e);
        }

        if (userResponseEntity.getStatusCode() != HttpStatus.OK || userResponseEntity.getBody() == null) {
            log.warn("User not found for ID: {}", userId);
            return getSubscriptionByIdFallback(id, new IllegalArgumentException("User not found"));
        }

        SuscriptionsResource suscriptionsResource = SuscriptionsResourceFromEntityAssembler.toResourceFromEntity(suscription);
        return ResponseEntity.ok(suscriptionsResource);
    }

    public ResponseEntity<SuscriptionsResource> getSubscriptionByIdFallback(Long id, Throwable t) {
        log.warn("Fallback method called for getSubscriptionById with ID: {}", id, t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new SuscriptionsResource(id, null, null, "PlanType unavailable"));
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getSubscriptionByUserIdFallback")
    @GetMapping(params = {"userId"})
    public ResponseEntity<?> getSubscriptionByUserId(@RequestParam("userId") Long userId) {
        log.info("Fetching subscription by user ID: {}", userId);
        Optional<Suscriptions> subscription = suscriptionsQueryService.getSubscriptionByUserId(userId);
        if (subscription.isEmpty()) {
            log.warn("Subscription not found for user ID: {}", userId);
            return ResponseEntity.notFound().build();
        }

        Suscriptions suscription = subscription.get();
        String token = tokenCacheService.getToken("default");
        ResponseEntity<User> userResponseEntity;
        try {
            userResponseEntity = userClient.getUserById(userId, token);
        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException for user ID: {}", userId, e);
            return getSubscriptionByUserIdFallback(userId, e);
        }

        if (userResponseEntity.getStatusCode() != HttpStatus.OK || userResponseEntity.getBody() == null) {
            log.warn("User not found for ID: {}", userId);
            return getSubscriptionByUserIdFallback(userId, new IllegalArgumentException("User not found"));
        }

        SuscriptionsResource suscriptionsResource = SuscriptionsResourceFromEntityAssembler.toResourceFromEntity(suscription);
        return ResponseEntity.ok(suscriptionsResource);
    }

    public ResponseEntity<?> getSubscriptionByUserIdFallback(Long userId, Throwable t) {
        log.warn("Fallback method called for getSubscriptionByUserId with user ID: {}", userId, t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new SuscriptionsResource(null, null, userId, "PlanType unavailable"));
    }

    @PostMapping("/create/sub")
    @Transactional
    public ResponseEntity<String> createSubscription(@RequestParam Long userId) {
        try {
            log.info("Creating subscription for userId: {} with plan: free", userId);

            // Set default plan to "free"
            PlanTypes planType = PlanTypes.free;
            Plan selectedPlan = planRepository.findByPlanType(planType)
                    .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

            // Create the subscription in the suscriptions database
            CreateSuscriptionsCommand command = new CreateSuscriptionsCommand(selectedPlan.getId(), userId);
            suscriptionsCommandService.handle(command);

            return ResponseEntity.ok("Subscription created successfully");
        } catch (Exception e) {
            log.error("Error creating subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating subscription");
        }
    }

    @PutMapping("/upgrade/subscription")
    @Transactional
    @CircuitBreaker(name = "default", fallbackMethod = "upgradeSubscriptionFallback")
    public ResponseEntity<String> upgradeSubscription(@RequestParam Long userId, @RequestParam String newPlanType) {
        try {
            log.info("Upgrade subscription for userId: {} to plan: {}", userId, newPlanType);

            // Retrieve the user
            ResponseEntity<User> userResponse = userClient.getUserById(userId, null);
            User user = userResponse.getBody();
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            // Retrieve the subscription
            Optional<Suscriptions> suscriptionOpt = suscriptionsQueryService.getSubscriptionByUserId(userId);
            if (suscriptionOpt.isEmpty()) {
                throw new IllegalArgumentException("Subscription not found");
            }
            Suscriptions suscription = suscriptionOpt.get();
            PlanTypes currentPlanType = PlanTypes.valueOf(String.valueOf(suscription.getPlan().getPlanType()));

            PlanTypes newPlanTypeEnum;
            try {
                newPlanTypeEnum = PlanTypes.valueOf(newPlanType);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid plan type");
            }

            log.info("Current plan type: {}", currentPlanType);
            log.info("New plan type: {}", newPlanTypeEnum);

            if (currentPlanType == newPlanTypeEnum) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is already at the specified plan level");
            }
            if ((currentPlanType == PlanTypes.free && (newPlanTypeEnum != PlanTypes.bronce && newPlanTypeEnum != PlanTypes.plata && newPlanTypeEnum != PlanTypes.oro)) ||
                    (currentPlanType == PlanTypes.bronce && (newPlanTypeEnum != PlanTypes.plata && newPlanTypeEnum != PlanTypes.oro)) ||
                    (currentPlanType == PlanTypes.plata && newPlanTypeEnum != PlanTypes.oro) ||
                    (currentPlanType == PlanTypes.oro)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid upgrade path");
            }

            // Call the payment service to create a subscription payment
            BigDecimal amount = new BigDecimal("100.00"); // Example amount
            ResponseEntity<Map<String, String>> paymentResponse = paymentClient.createSubscriptionPayment(amount, newPlanTypeEnum.name());

            if (!paymentResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment failed, subscription not upgraded");
            }

            // Extract the payment link from the response
            String paymentLink = paymentResponse.getBody().get("approval_url");

            ResponseEntity<String> responseEntity = ResponseEntity.ok(Map.of("paymentLink", paymentLink).toString());

            int allowedSportspaces;
            switch (newPlanTypeEnum) {
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

            new Thread(() -> {
                try {
                    // Simulate waiting for payment processing (e.g., sleep for a few seconds)
                    Thread.sleep(20000); // Adjust the sleep time as needed

                    // Check payment status using userId
                    boolean paymentApproved = paymentClient.checkPaymentStatus(String.valueOf(userId));

                    if (paymentApproved) {
                        // Update the user's plan and allowedSportspaces in the users database
                        userClient.updateUserPlanAndSportspaces(userId, newPlanTypeEnum.name(), allowedSportspaces);

                        // Update the subscription in the suscriptions database
                        Plan selectedPlan = planRepository.findByPlanType(newPlanTypeEnum)
                                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
                        suscription.setPlan(selectedPlan);
                        suscriptionsCommandService.updateSuscription(suscription);
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
            log.error("ResourceAccessException for user ID: {}", userId, e);
            throw e; // Rethrow the exception to let the CircuitBreaker handle it
        } catch (Exception e) {
            throw e; // Rethrow the exception to let the CircuitBreaker handle it
        }
    }

    public ResponseEntity<String> upgradeSubscriptionFallback(Long userId, String newPlanType, Throwable t) {
        log.error("Fallback method called for upgradeSubscription with user ID: {} and new plan type: {}", userId, newPlanType, t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Subscription service unavailable");
    }
}