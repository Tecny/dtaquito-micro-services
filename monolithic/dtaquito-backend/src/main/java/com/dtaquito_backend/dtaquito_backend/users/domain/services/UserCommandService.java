package com.dtaquito_backend.dtaquito_backend.users.domain.services;

import java.util.Optional;

import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignInCommand;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.events.UserCreatedEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserCommandService {

    //Optional<ImmutablePair<User, String>> handle(CreateUserCommand command);
    Optional<User> handle(SignUpCommand command);
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);
    Optional<User> updateUser(Long id, SignUpCommand command);
    void handleUserCreatedEvent(UserCreatedEvent event);
    User save(User user);
    Optional<User> findById(Long id);
}
