package com.dtaquito_backend.dtaquito_backend.users.domain.services;


import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.queries.GetAllUserByNameQuery;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.queries.GetUserByIdQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserQueryService {

    List<User> handle(GetAllUserByNameQuery query);

    Optional<User> handle(GetUserByIdQuery query);

    List<User> getAllUsers();

    String getUserRoleByUserId(Long userId);

    Optional<User> getUserById(Long userId);

    Optional<User> getUserByEmailAndPassword (String email, String password);

}
