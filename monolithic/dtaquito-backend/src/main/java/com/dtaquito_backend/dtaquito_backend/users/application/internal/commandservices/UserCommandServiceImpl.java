package com.dtaquito_backend.dtaquito_backend.users.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.hashing.HashingService;
import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignInCommand;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.events.UserCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserCommandService;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.RoleRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TokenService tokenService;

    public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository, ApplicationEventPublisher applicationEventPublisher,
                                  HashingService hashingService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
    }

    @Override
    public Optional<User> updateUser(Long id, SignUpCommand command) {
        var user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String encryptedPassword = hashingService.encode(command.password());
        SignUpCommand updatedCommand = new SignUpCommand(command.name(), command.email(), encryptedPassword, command.roles(), command.bankAccount());
        user.update(updatedCommand);
        var updatedUser = userRepository.save(user);
        return Optional.of(updatedUser);
    }

    @Override
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        System.out.println("UserCreatedEvent received for user ID: " + event.getUserId());
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (command.roles() == null || command.roles().isEmpty()) {
            throw new IllegalArgumentException("Role names must not be null or empty");
        }
        var roleTypes = command.roles();
        var roles = new ArrayList<Role>();
        for (Role roleType : roleTypes) {
            Optional<Role> role = roleRepository.findByRoleType(roleType.getRoleType());
            if (role.isEmpty()) {
                throw new IllegalArgumentException("Role not found: " + roleType);
            }
            roles.add(role.get());
        }

        if (command.email() == null || command.email().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }

        // Set default plan to "free"
        var user = new User(command.name(), command.email(), hashingService.encode(command.password()), roles.get(0), "free", command.bankAccount());
        var createdUser = userRepository.save(user);

        UserCreatedEvent event = new UserCreatedEvent(this, createdUser.getId());
        applicationEventPublisher.publishEvent(event);

        return Optional.of(createdUser);
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!hashingService.matches(command.password(), user.getPassword()))
            throw new RuntimeException("Invalid password");
        var token = tokenService.generateToken(user.getEmail(), String.valueOf(user.getId()));
        return Optional.of(ImmutablePair.of(user, token));
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}