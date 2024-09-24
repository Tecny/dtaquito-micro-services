package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest;


import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_backend.dtaquito_backend.deposit.infrastructure.persistance.jpa.DepositRepository;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetAllSportSpacesByNameQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByIdQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByUserId;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.GameMode;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesCommandService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesQueryService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportSpacesRepository;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsQueryService;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.CreateSportSpacesResource;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.SportSpacesResource;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform.CreateSportSpacesCommandFromResourceAssembler;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform.SportSpacesResourceFromEntityAssembler;
import com.paypal.api.payments.Payout;
import com.paypal.api.payments.PayoutBatch;
import com.paypal.api.payments.PayoutItem;
import com.paypal.api.payments.PayoutSenderBatchHeader;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/sport-spaces", produces = MediaType.APPLICATION_JSON_VALUE)
public class SportSpacesController {

    private final SportSpacesCommandService sportSpacesCommandService;

    private final SportSpacesQueryService sportSpacesQueryService;
    private final UserQueryService userQueryService;
    private final SuscriptionsQueryService suscriptionsQueryService;


    public SportSpacesController(SportSpacesCommandService sportSpacesCommandService, SportSpacesQueryService sportSpacesQueryService, UserQueryService userQueryService, SuscriptionsQueryService suscriptionsQueryService) {
        this.sportSpacesCommandService = sportSpacesCommandService;
        this.sportSpacesQueryService = sportSpacesQueryService;
        this.suscriptionsQueryService = suscriptionsQueryService;
        this.userQueryService = userQueryService;
    }

    private static final Logger logger = LoggerFactory.getLogger(SportSpacesController.class);

    @PostMapping
    public ResponseEntity<?> createSportSpaces(@RequestBody CreateSportSpacesResource resource) {
        try {
            Long userId = resource.userId();
            logger.info("Creating sport spaces for user with ID: {}", userId);
            RoleTypes userRole;
            PlanTypes userSubscriptionPlan;
            try {
                userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(userId));
                Suscriptions suscription = suscriptionsQueryService.getSubscriptionByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Subscription not found for user"));
                userSubscriptionPlan = suscription.getPlan().getPlanType();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @GetMapping("{id}")
    public ResponseEntity<SportSpacesResource> getUserById(@PathVariable Long id) {
        Optional<SportSpaces> sportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByIdQuery(id));
        return sportSpaces.map(source -> ResponseEntity.ok(SportSpacesResourceFromEntityAssembler.toResourceFromEntity(source))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<List<SportSpacesResource>> getAllUsersByName(String name){

        var getAllSportSpacesByNameQuery = new GetAllSportSpacesByNameQuery(name);
        var sportSpaces = sportSpacesQueryService.handle(getAllSportSpacesByNameQuery);
        if (sportSpaces.isEmpty()) return ResponseEntity.notFound().build();
        var sportSpacesResources = sportSpaces.stream().map(SportSpacesResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(sportSpacesResources);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SportSpacesResource>> getAllSportSpaces(){
        var sportSpaces = sportSpacesQueryService.getAllSportSpaces();
        var sportSpacesResources = sportSpaces.stream().map(SportSpacesResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(sportSpacesResources);
    }

    @GetMapping(params = {"name"})
    public ResponseEntity<?> getUsersWithParameters(@RequestParam Map<String, String> params) {

        if (params.containsKey("name")) {
            return getAllUsersByName(params.get("name"));
        }
        else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<SportSpacesResource> updateSportSpaces(@PathVariable Long id, @RequestBody CreateSportSpacesResource resource) {
        RoleTypes userRole;
        try {
            userRole = RoleTypes.valueOf(userQueryService.getUserRoleByUserId(resource.userId()));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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

    @GetMapping(params = {"userId"})
    public ResponseEntity<List<SportSpacesResource>> getSportSpacesByUserId(@RequestParam("userId") Long userId) {
        List<SportSpaces> sportSpaces = sportSpacesQueryService.handle(new GetSportSpacesByUserId(userId));
        if (sportSpaces.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            List<SportSpacesResource> sportSpacesResources = sportSpaces.stream()
                    .map(SportSpacesResourceFromEntityAssembler::toResourceFromEntity)
                    .collect(toList());
            return ResponseEntity.ok(sportSpacesResources);
        }
    }
}