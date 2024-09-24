package com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa;


import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByName(String name);
    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);
}