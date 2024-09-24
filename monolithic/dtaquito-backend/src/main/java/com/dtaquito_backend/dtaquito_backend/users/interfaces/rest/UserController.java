package com.dtaquito_backend.dtaquito_backend.users.interfaces.rest;

import java.util.*;

import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.SignUpResource;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.suscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_backend.dtaquito_backend.payments.interfaces.rest.PaymentsController;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.queries.GetAllUserByNameQuery;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.queries.GetUserByIdQuery;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserCommandService;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserQueryService;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.resources.UserResource;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.transform.UserResourceFromEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Users Controller")
@Slf4j
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final SuscriptionsCommandService suscriptionsCommandService;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public UserController(UserCommandService userCommandService, UserQueryService userQueryService, SuscriptionsCommandService suscriptionsCommandService, PlanRepository planRepository,
                          UserRepository userRepository) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.suscriptionsCommandService = suscriptionsCommandService;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody SignUpResource resource, HttpServletRequest request) {
        try {
            // Validate roles
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

            // Create user
            var user = userCommandService.handle(SignUpCommandFromResourceAssembler.toCommandFromResource(resource));
            if (user.isPresent()) {
                userRepository.save(user.get());
                request.getSession().setAttribute("userId", user.get().getId().toString());

                // Set default plan to "free"
                PlanTypes planType = PlanTypes.free;
                Plan selectedPlan = planRepository.findByPlanType(planType)
                        .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
                CreateSuscriptionsCommand command = new CreateSuscriptionsCommand(selectedPlan.getId(), user.get().getId(), null);
                suscriptionsCommandService.handle(command);

                // Update allowed_sportspaces based on plan
                log.info("Updating allowed_sportspaces for user: {}", user.get().getId());
                user.get().setPlan(planType.name().toLowerCase());
                user.get().setAllowedSportspacesBasedOnPlan();
                userRepository.save(user.get());
                log.info("Updated allowed_sportspaces for user: {} to {}", user.get().getId(), user.get().getAllowedSportspaces());

                return ResponseEntity.ok(Map.of(
                        "message", "User created with default free plan",
                        "userId", user.get().getId().toString()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User registration failed");
            }
        } catch (Exception e) {
            log.error("Error creating user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user");
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long id){
        Optional<User> user = userQueryService.handle(new GetUserByIdQuery(id));
        return user.map(source -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(source))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<List<UserResource>> getAllUsersByName(String name){
        var getAllUserByNameQuery = new GetAllUserByNameQuery(name);
        var users = userQueryService.handle(getAllUserByNameQuery);
        if (users.isEmpty()) return ResponseEntity.notFound().build();
        var userResources = users.stream().map(UserResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(userResources);
    }

    @GetMapping(params = {"!name", "!id", "!email", "!password", "!role"})
    public ResponseEntity<List<UserResource>> getAllUsers(){
        var users = userQueryService.getAllUsers();
        var userResources = users.stream().map(UserResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(userResources);
    }

    @GetMapping(params = "name")
    public ResponseEntity<?> getUsersWithParameters(@RequestParam Map<String, String> params){
        if (params.containsKey("name")) {
            return getAllUsersByName(params.get("name"));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<UserResource> updateUser(@PathVariable Long id, @RequestBody SignUpResource resource) {
        Optional<User> user = userCommandService.updateUser(id, SignUpCommandFromResourceAssembler.toCommandFromResource((resource)));
        return user.map(source -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(source))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(params = {"email", "password"})
    public ResponseEntity<UserResource> getUserByEmailAndPassword(@RequestParam("email") String email, @RequestParam("password") String password){
        Optional<User> user = userQueryService.getUserByEmailAndPassword(email, password);
        return user.map(source -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(source))).orElseGet(() -> ResponseEntity.notFound().build());
    }
}