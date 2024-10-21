package com.dtaquito_micro_services.user_service.users.domain.services;

import com.dtaquito_micro_services.user_service.iam.domain.model.commands.SignInCommand;
import com.dtaquito_micro_services.user_service.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_micro_services.user_service.users.domain.model.aggregates.User;
import com.dtaquito_micro_services.user_service.users.domain.model.events.UserCreatedEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.Optional;

public interface UserCommandService {

    //Optional<ImmutablePair<User, String>> handle(CreateUserCommand command);
    Optional<User> handle(SignUpCommand command);
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);
    Optional<User> updateUser(Long id, SignUpCommand command);
    void handleUserCreatedEvent(UserCreatedEvent event);
    User save(User user);
    Optional<User> findById(Long id);
    void updateUserPlanAndSportspaces(Long id, Map<String, Object> updates);
    void addCredits(Long id, Integer credits);
    void deductCredits(Long id, Integer credits);
}
