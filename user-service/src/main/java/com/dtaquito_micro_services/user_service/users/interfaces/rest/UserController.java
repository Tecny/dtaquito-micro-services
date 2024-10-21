package com.dtaquito_micro_services.user_service.users.interfaces.rest;

import com.dtaquito_micro_services.user_service.iam.interfaces.rest.resources.SignUpResource;
import com.dtaquito_micro_services.user_service.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.dtaquito_micro_services.user_service.users.client.SuscriptionClient;
import com.dtaquito_micro_services.user_service.users.domain.model.aggregates.User;
import com.dtaquito_micro_services.user_service.users.domain.model.events.UserCreatedEvent;
import com.dtaquito_micro_services.user_service.users.domain.model.queries.GetUserByIdQuery;

import com.dtaquito_micro_services.user_service.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_micro_services.user_service.users.domain.services.UserCommandService;
import com.dtaquito_micro_services.user_service.users.domain.services.UserQueryService;
import com.dtaquito_micro_services.user_service.users.infrastructure.persistance.jpa.UserRepository;
import com.dtaquito_micro_services.user_service.users.interfaces.rest.resources.UserResource;
import com.dtaquito_micro_services.user_service.users.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Users Controller")
@Slf4j
    public class UserController {

        private final UserCommandService userCommandService;
        private final UserQueryService userQueryService;
        private final UserRepository userRepository;
        private final SuscriptionClient suscriptionClient;
        private final ApplicationEventPublisher applicationEventPublisher;


        @Autowired
        public UserController(UserCommandService userCommandService, UserQueryService userQueryService,
                              UserRepository userRepository, SuscriptionClient suscriptionClient,
                              ApplicationEventPublisher applicationEventPublisher) {
            this.userCommandService = userCommandService;
            this.userQueryService = userQueryService;
            this.userRepository = userRepository;
            this.suscriptionClient = suscriptionClient;
            this.applicationEventPublisher = applicationEventPublisher;
        }

        @PostMapping
        @CircuitBreaker(name = "default", fallbackMethod = "createUserFallback")
        public ResponseEntity<?> createUser(@RequestBody SignUpResource resource, HttpServletRequest request) {
            try {
                // Validate roles
                if (resource.roles() == null || resource.roles().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Roles cannot be null or empty");
                }
                for (String role : resource.roles()) {
                    if (!EnumUtils.isValidEnum(RoleTypes.class, role)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role: " + role);
                    }
                }

                // Check if user with the same email already exists
                Optional<User> existingUser = userRepository.findByEmail(resource.email());
                if (existingUser.isPresent()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this email already exists");
                }

                var user = userCommandService.handle(SignUpCommandFromResourceAssembler.toCommandFromResource(resource));
                var createdUser = userRepository.save(user.get());

                try{
                    suscriptionClient.createSubscription(createdUser.getId());
                    log.info("Updating allowed_sportspaces for user: {}", createdUser.getId());
                    createdUser.setAllowedSportspacesBasedOnPlan();
                    userRepository.save(createdUser);
                    log.info("Updated allowed_sportspaces for user: {} to {}", createdUser.getAllowedSportspaces());

                    UserCreatedEvent event = new UserCreatedEvent(this, createdUser.getId());
                    applicationEventPublisher.publishEvent(event);
                    return ResponseEntity.ok(Map.of(
                            "message", "User created with default free plan",
                            "userId", user.get().getId().toString()
                    ));
                } catch (ResourceAccessException e) {
                    userRepository.delete(createdUser);
                    return createUserFallback(resource, request, e);
                }
            } catch (Exception e) {
                log.error("Error creating user: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user");
            }
        }

        public ResponseEntity<?> createUserFallback(@RequestBody SignUpResource resource, HttpServletRequest request, Throwable t) {
            log.error("Fallback triggered for createUser: ", t);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Subscription service unavailable");
        }

        @GetMapping("{id}")
        public ResponseEntity<UserResource> getUserById(@PathVariable Long id){
            Optional<User> user = userQueryService.handle(new GetUserByIdQuery(id));
            return user.map(source -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(source))).orElseGet(() -> ResponseEntity.notFound().build());
        }

        @GetMapping(params = "email")
        public ResponseEntity<UserResource> getUserByEmail(@RequestParam("email") String email) {
            Optional<User> user = userQueryService.getUserByEmail(email);
            return user.map(source -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(source)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        @PutMapping("{id}")
        public ResponseEntity<UserResource> updateUser(@PathVariable Long id, @RequestBody SignUpResource resource) {
            Optional<User> user = userCommandService.updateUser(id, SignUpCommandFromResourceAssembler.toCommandFromResource((resource)));
            return user.map(source -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(source))).orElseGet(() -> ResponseEntity.notFound().build());
        }

//        @GetMapping(params = {"email", "password"})
//        public ResponseEntity<UserResource> getUserByEmailAndPassword(@RequestParam("email") String email, @RequestParam("password") String password){
//            Optional<User> user = userQueryService.getUserByEmailAndPassword(email, password);
//            return user.map(source -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(source))).orElseGet(() -> ResponseEntity.notFound().build());
//        }

        @PutMapping("/update/{id}")
        public ResponseEntity<Void> updateUserPlanAndSportspaces(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
            userCommandService.updateUserPlanAndSportspaces(id, updates);
            return ResponseEntity.noContent().build();
        }

        @PutMapping("/update/credits/{id}")
        public ResponseEntity<Void> updateCredits(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
            Integer credits = request.get("credits");
            if (credits == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            userCommandService.addCredits(id, credits);
            return ResponseEntity.noContent().build();
        }

        // UserController.java
        @PutMapping("/deduct/credits/{id}")
        public ResponseEntity<Void> deductCredits(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
            Integer credits = request.get("credits");
            if (credits == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            userCommandService.deductCredits(id, credits);
            return ResponseEntity.noContent().build();
        }
}