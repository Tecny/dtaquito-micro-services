package com.dtaquito_backend.dtaquito_backend.dtaquito.domain.service;

import com.dtaquito_backend.dtaquito_backend.config.TestSecurityConfig;
import com.dtaquito_backend.dtaquito_backend.iam.application.internal.outboundservices.hashing.HashingService;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.suscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_backend.dtaquito_backend.users.application.internal.commandservices.UserCommandServiceImpl;
import com.dtaquito_backend.dtaquito_backend.users.application.internal.queryservices.UserQueryServiceImpl;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.RoleRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    private UserQueryServiceImpl userQueryService;

    @Mock
    private SuscriptionsCommandService suscriptionsCommandService;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    private User user1;
    private Role defaultRole;


    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("Juan");
        user1.setEmail("juan@gmail.com");
        user1.setPassword("123456");
        user1.setBankAccount("123456789");
        user1.setAllowedSportspaces(0);
        user1.setCredits(BigDecimal.ZERO);

        defaultRole = Role.getDefaultRole();
        user1.setRole(defaultRole);

        Plan freePlan = new Plan();
        freePlan.setPlanType(PlanTypes.free);

        lenient().when(roleRepository.findByRoleType(defaultRole.getRoleType())).thenReturn(Optional.of(defaultRole)); // Mock roleRepository
        lenient().when(hashingService.encode(any(CharSequence.class))).thenReturn("hashedPassword"); // Mock hashingService
    }

    @DisplayName("Test for create new user")
    @Test
    void testCreateUser() {
        SignUpCommand signUpCommand = new SignUpCommand(
                user1.getName(),
                user1.getEmail(),
                user1.getPassword(),
                List.of(defaultRole),
                user1.getBankAccount()
        );

        when(userRepository.save(any(User.class))).thenReturn(user1);

        Optional<User> user = userCommandService.handle(signUpCommand);

        assertTrue(user.isPresent());
        assertEquals(user1.getEmail(), user.get().getEmail());
    }

    @DisplayName("Test for update user resource")
    @Test
    void testUpdateUser() {
        SignUpCommand signUpCommand = new SignUpCommand(
                user1.getName(),
                user1.getEmail(),
                user1.getPassword(),
                List.of(defaultRole),
                user1.getBankAccount()
        );

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        when(userRepository.save(any(User.class))).thenReturn(user1);

        Optional<User> user = userCommandService.updateUser(user1.getId(), signUpCommand);

        assertTrue(user.isPresent());
        assertEquals(user1.getEmail(), user.get().getEmail());
    }
}