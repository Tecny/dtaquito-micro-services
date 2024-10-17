package com.dtaquito_micro_services.sport_service.sportspaces.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.entities.Sport;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects.SportTypes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRepository extends JpaRepository<Sport, Long> {
    boolean existsBySportType(SportTypes sportType);
}