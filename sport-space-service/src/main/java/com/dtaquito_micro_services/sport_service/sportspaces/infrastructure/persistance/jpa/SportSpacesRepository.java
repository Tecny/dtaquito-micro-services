package com.dtaquito_micro_services.sport_service.sportspaces.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.aggregates.SportSpaces;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SportSpacesRepository extends JpaRepository<SportSpaces, Long> {

    List<SportSpaces> findAllByName(String name);
    List<SportSpaces> findByUserId(Long userId);
}
