package com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Date;
import java.util.List;

public interface RoomsRepository extends JpaRepository<Rooms, Long> {
    List<Rooms> findBySportSpaceAndOpeningDate(SportSpaces sportSpace, Date openingDate);
}