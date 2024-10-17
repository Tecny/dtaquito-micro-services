package com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest;

import com.dtaquito_micro_services.sport_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.sport_service.sportspaces.client.SuscriptionClient;
import com.dtaquito_micro_services.sport_service.sportspaces.client.UserClient;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.entities.User;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.queries.GetSportSpacesByIdQuery;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.queries.GetSportSpacesByUserId;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects.PlanTypes;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects.RoleTypes;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.services.SportSpacesCommandService;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.services.SportSpacesQueryService;
import com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.resources.CreateSportSpacesResource;
import com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.resources.SportSpacesResource;
import com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.transform.CreateSportSpacesCommandFromResourceAssembler;
import com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.transform.SportSpacesResourceFromEntityAssembler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;

import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/sports", produces = MediaType.APPLICATION_JSON_VALUE)
public class SportSpacesController {

    private final SportSpacesCommandService sportSpacesCommandService;
    private final SportSpacesQueryService sportSpacesQueryService;
    private final SuscriptionClient suscriptionClient;
    private final UserClient userClient;
    private final TokenCacheService tokenCacheService;

    public SportSpacesController(SportSpacesCommandService sportSpacesCommandService, SportSpacesQueryService sportSpacesQueryService,
                                 SuscriptionClient suscriptionClient, UserClient userClient, TokenCacheService tokenCacheService) {
        this.sportSpacesCommandService = sportSpacesCommandService;
        this.sportSpacesQueryService = sportSpacesQueryService;
        this.suscriptionClient = suscriptionClient;
        this.userClient = userClient;
        this.tokenCacheService = tokenCacheService;
    }

    private static final Logger logger = LoggerFactory.getLogger(SportSpacesController.class);

    @PostMapping("/create/sport-spaces")
    public ResponseEntity<?> createSportSpaces(@RequestBody CreateSportSpacesResource resource) {
        try {
            Long userId = resource.userId();
            logger.info("Creating sport spaces for user with ID: {}", userId);
            RoleTypes userRole;
            PlanTypes userSubscriptionPlan;

            try {
                String token = tokenCacheService.getToken("default");
                ResponseEntity<User> userResponse = userClient.getUserById(userId, token);
                if (userResponse.getStatusCode() == HttpStatus.OK && userResponse.getBody() != null) {
                    User user = userResponse.getBody();
                    userRole = RoleTypes.valueOf(user.getRoleType());
                } else {
                    throw new IllegalArgumentException("User not found");
                }

                ResponseEntity<?> response = suscriptionClient.getSuscriptionsByUserId(userId);
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                    userSubscriptionPlan = PlanTypes.valueOf((String) responseBody.get("planType"));
                } else {
                    throw new IllegalArgumentException("Subscription not found for user");
                }
            } catch (IllegalArgumentException e) {
                logger.error("Invalid user role or subscription plan", e);
                throw new IllegalArgumentException("Invalid user role or subscription plan", e);
            }
            if (!userRole.equals(RoleTypes.P) || !(userSubscriptionPlan.equals(PlanTypes.oro) || userSubscriptionPlan.equals(PlanTypes.plata) || userSubscriptionPlan.equals(PlanTypes.bronce))) {
                logger.error("User role must be 'P' and subscription plan must be 'oro', 'plata', or 'bronce'");
                throw new IllegalArgumentException("User role must be 'P' and subscription plan must be 'oro', 'plata', or 'bronce'");
            }

            List<SportSpaces> userSportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByUserId(userId));
            int maxSportSpacesAllowed = switch (userSubscriptionPlan) {
                case oro -> 3;
                case plata -> 2;
                case bronce -> 1;
                default -> 0;
            };

            if (userSportSpaces.size() >= maxSportSpacesAllowed) {
                String errorMessage = String.format("You have reached the maximum number of sport spaces allowed for your %s plan.", userSubscriptionPlan.name());
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            Optional<SportSpaces> sportSpaces = sportSpacesCommandService.handle(userId, CreateSportSpacesCommandFromResourceAssembler.toCommandFromResource((resource)));
            return sportSpaces.map(s -> ResponseEntity.ok(SportSpacesResourceFromEntityAssembler.toResourceFromEntity(s)))
                    .orElseThrow(() -> new IllegalArgumentException("Failed to create sport space"));
        } catch (Exception e) {
            logger.error("Error creating sport space", e);
            return createSportSpacesFallback(resource, e);
        }
    }

    public ResponseEntity<?> createSportSpacesFallback(CreateSportSpacesResource resource, Throwable t) {
        logger.error("Fallback method for createSportSpaces", t);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Service is currently unavailable. Please try again later."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }


    public ResponseEntity<SportSpacesResource> getSportSpaceByIdFallback(Long id, Throwable t) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new SportSpacesResource(
                        id,
                        "Service Unavailable",
                        null,
                        "Service Unavailable",
                        "Service Unavailable",
                        0.0,
                        null,
                        "Service Unavailable",
                        null,
                        "00:00",
                        "00:00",
                        null,
                        0
                ));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SportSpacesResource>> getAllSportSpaces(){
        var sportSpaces = sportSpacesQueryService.getAllSportSpaces();
        var sportSpacesResources = sportSpaces.stream().map(SportSpacesResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(sportSpacesResources);
    }

    @PutMapping("{id}")
    public ResponseEntity<SportSpacesResource> updateSportSpaces(@PathVariable Long id, @RequestBody CreateSportSpacesResource resource) {
        RoleTypes userRole;
        try {
            String token = tokenCacheService.getToken("default");
            ResponseEntity<User> userResponse = userClient.getUserById(resource.userId(), token);
            if (userResponse.getStatusCode() == HttpStatus.OK && userResponse.getBody() != null) {
                User user = userResponse.getBody();
                userRole = RoleTypes.valueOf(user.getRoleType());
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (ResourceAccessException e) {
            return updateSportSpacesFallback(id, resource, e);
        }
        if (!userRole.equals(RoleTypes.P)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<SportSpaces> existingSportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByIdQuery(id));
        if (!existingSportSpaces.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<SportSpaces> sportSpaces = sportSpacesCommandService.handleUpdate(id, CreateSportSpacesCommandFromResourceAssembler.toCommandFromResource((resource)));
        return sportSpaces.map(source -> new ResponseEntity<>(SportSpacesResourceFromEntityAssembler.toResourceFromEntity(source), HttpStatus.OK)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<SportSpacesResource> updateSportSpacesFallback(Long id, CreateSportSpacesResource resource, Throwable t) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new SportSpacesResource(
                        id,
                        "Service Unavailable",
                        null,
                        "Service Unavailable",
                        "Service Unavailable",
                        0.0,
                        null,
                        "Service Unavailable",
                        null,
                        "00:00",
                        "00:00",
                        null,
                        0
                ));
    }

    @GetMapping("{id}")
    public ResponseEntity<SportSpacesResource> getSportSpaceById(@PathVariable Long id) {
        Optional<SportSpaces> sportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByIdQuery(id));
        if (sportSpaces.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SportSpaces sportSpace = sportSpaces.get();
        Long userId = sportSpace.getUserId();
        String token = tokenCacheService.getToken("default");
        ResponseEntity<User> userResponse;
        try {
            userResponse = userClient.getUserById(userId, token);
        } catch (ResourceAccessException e) {
            return getSportSpaceByIdFallback(id, e);
        }

        if (userResponse.getStatusCode() != HttpStatus.OK || userResponse.getBody() == null) {
            return getSportSpaceByIdFallback(id, new ResourceAccessException("User service unavailable"));
        }

        SportSpacesResource resource = SportSpacesResourceFromEntityAssembler.toResourceFromEntity(sportSpace);
        return ResponseEntity.ok(resource);
    }

    @GetMapping(params = {"userId"})
    public ResponseEntity<List<SportSpacesResource>> getSportSpacesByUserId(@RequestParam("userId") Long userId) {
        List<SportSpaces> sportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByUserId(userId));
        if (sportSpaces.isEmpty()) {
            return ResponseEntity.notFound().build();

        }
        String token = tokenCacheService.getToken("default");
        ResponseEntity<User> userResponse;
        try {
            userResponse = userClient.getUserById(userId, token);
        } catch (ResourceAccessException e) {
            return getSportSpacesByUserIdFallback(userId, e);
        }

        if (userResponse.getStatusCode() != HttpStatus.OK || userResponse.getBody() == null) {
            return getSportSpacesByUserIdFallback(userId, new ResourceAccessException("User service unavailable"));
        }

        List<SportSpacesResource> resources = sportSpaces.stream().map(SportSpacesResourceFromEntityAssembler::toResourceFromEntity).collect(toList());
        return ResponseEntity.ok(resources);
    }

    public ResponseEntity<List<SportSpacesResource>> getSportSpacesByUserIdFallback(Long userId, Throwable t) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.singletonList(new SportSpacesResource(
                        null,
                        "Service Unavailable",
                        null,
                        "Service Unavailable",
                        "Service Unavailable",
                        0.0,
                        null,
                        "Service Unavailable",
                        userId,
                        "00:00",
                        "00:00",
                        null,
                        0
                )));
    }
}