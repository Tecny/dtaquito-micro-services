package com.dtaquito_micro_services.rooms_service.rooms.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Date;
import java.util.List;

public interface RoomsRepository extends JpaRepository<Rooms, Long> {
    List<Rooms> findBySportSpaceIdAndOpeningDate(Long sportSpaceId, Date openingDate);
}