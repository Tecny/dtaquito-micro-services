package com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SportSpacesRepository extends JpaRepository<SportSpaces, Long> {

    List<SportSpaces> findAllByName(String name);
    List<SportSpaces> findByUserId(Long userId);
}
