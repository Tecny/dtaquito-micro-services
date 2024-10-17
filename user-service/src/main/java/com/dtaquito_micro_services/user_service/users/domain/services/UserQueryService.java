package com.dtaquito_micro_services.user_service.users.domain.services;


import com.dtaquito_micro_services.user_service.users.domain.model.aggregates.User;
import com.dtaquito_micro_services.user_service.users.domain.model.queries.GetAllUserByNameQuery;
import com.dtaquito_micro_services.user_service.users.domain.model.queries.GetUserByIdQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserQueryService {

    List<User> handle(GetAllUserByNameQuery query);

    Optional<User> handle(GetUserByIdQuery query);

    List<User> getAllUsers();

    Optional<User> getUserByEmail(String email);

    String getUserRoleByUserId(Long userId);

    Optional<User> getUserById(Long userId);

    Optional<User> getUserByEmailAndPassword (String email, String password);

}
