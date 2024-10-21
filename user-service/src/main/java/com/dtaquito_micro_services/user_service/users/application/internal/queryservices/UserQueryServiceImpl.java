package com.dtaquito_micro_services.user_service.users.application.internal.queryservices;

import com.dtaquito_micro_services.user_service.users.domain.model.aggregates.User;
import com.dtaquito_micro_services.user_service.users.domain.model.queries.GetAllUserByNameQuery;
import com.dtaquito_micro_services.user_service.users.domain.model.queries.GetUserByIdQuery;
import com.dtaquito_micro_services.user_service.users.domain.services.UserQueryService;
import com.dtaquito_micro_services.user_service.users.infrastructure.persistance.jpa.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> handle (GetAllUserByNameQuery query) {
        return userRepository.findAllByName(query.name());
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Override
    public String getUserRoleByUserId(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(u -> u.getRole().getRoleType().name()).orElse(null);
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        Long userId = query.id();
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new NoSuchElementException("User with ID " + userId + " not found");
        }
        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return handle(new GetUserByIdQuery(userId));
    }

    @Override
    public Optional<User> getUserByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }
}