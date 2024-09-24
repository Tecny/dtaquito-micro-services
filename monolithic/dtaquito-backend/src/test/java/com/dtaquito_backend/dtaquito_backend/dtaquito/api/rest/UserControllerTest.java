package com.dtaquito_backend.dtaquito_backend.dtaquito.api.rest;

import com.dtaquito_backend.dtaquito_backend.config.TestSecurityConfig;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.SignUpResource;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.suscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_backend.dtaquito_backend.users.application.internal.commandservices.UserCommandServiceImpl;
import com.dtaquito_backend.dtaquito_backend.users.application.internal.queryservices.UserQueryServiceImpl;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.queries.GetUserByIdQuery;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.dtaquito_backend.dtaquito_backend.users.interfaces.rest.UserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserCommandServiceImpl userCommandService;

    @MockBean
    private UserQueryServiceImpl userQueryService;

    @MockBean
    private SuscriptionsCommandService suscriptionsCommandService;

    @MockBean
    private PlanRepository planRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user1;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("Juan");
        user1.setEmail("juan@gmail.com");
        user1.setPassword("123456");
        user1.setRole(Role.getDefaultRole());
        user1.setBankAccount("123456789");
        user1.setAllowedSportspaces(0);
        user1.setCredits(BigDecimal.ZERO);

        Plan freePlan = new Plan();
        freePlan.setPlanType(PlanTypes.free);

        when(planRepository.findByPlanType(PlanTypes.free)).thenReturn(Optional.of(freePlan));
        when(userQueryService.getUserById(user1.getId())).thenReturn(Optional.of(user1));
    }

    @Test
    void testCreateUser() throws Exception {
        SignUpResource createUserResource = new SignUpResource(
                "Juan", "juan@gmail.com", "123456", List.of("R"), "123456789");

        when(userRepository.findByEmail(createUserResource.email())).thenReturn(Optional.empty());
        when(planRepository.findByPlanType(PlanTypes.free)).thenReturn(Optional.of(new Plan(PlanTypes.free)));

        when(userCommandService.handle(any(SignUpCommand.class))).thenAnswer(invocation -> {
            SignUpCommand command = invocation.getArgument(0);
            User user = new User();
            user.setId(1L);
            user.setName(command.name());
            user.setEmail(command.email());
            user.setPassword(command.password());
            user.setRole(Role.getDefaultRole());
            user.setAllowedSportspaces(0);
            user.setPlan(String.valueOf(PlanTypes.free));
            user.setBankAccount(command.bankAccount());
            user.setCredits(BigDecimal.ZERO);
            return Optional.of(user);
        });

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User created with default free plan"))
                .andExpect(jsonPath("$.userId").value("1"));
    }

    @Test
    void testGetUserById() throws Exception {

        when(userQueryService.handle(any(GetUserByIdQuery.class))).thenReturn(Optional.of(user1));

        mockMvc.perform(get("/api/v1/users/{id}", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(user1.getName()))
                .andExpect(jsonPath("$.email").value(user1.getEmail()))
                .andExpect(jsonPath("$.password").value(user1.getPassword()))
                .andExpect(jsonPath("$.roleType").value(user1.getRole().getRoleType().name().trim()))
                .andExpect(jsonPath("$.bankAccount").value(user1.getBankAccount()));
    }

    @Test
    void testUpdateUser() throws Exception {
        SignUpResource updatedUserResource = new SignUpResource(
                "Juan Updated", "juan_updated@gmail.com",
                "newpassword", List.of("R"), "123456789");

        User updatedUser = User.builder()
                .id(user1.getId())
                .name(updatedUserResource.name())
                .email(updatedUserResource.email())
                .password(updatedUserResource.password())
                .role(Role.getDefaultRole())
                .allowedSportspaces(0)
                .plan("free")
                .bankAccount("123456789")
                .credits(BigDecimal.ZERO).build();

        when(userCommandService.updateUser(any(Long.class), any(SignUpCommand.class)))
                .thenReturn(Optional.of(updatedUser));

        mockMvc.perform(put("/api/v1/users/{id}", user1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedUser.getName()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()))
                .andExpect(jsonPath("$.password").value(updatedUser.getPassword()))
                .andExpect(jsonPath("$.roleType").value(updatedUser.getRole().getRoleType().name().trim()))
                .andExpect(jsonPath("$.bankAccount").value(updatedUser.getBankAccount()));
    }
}