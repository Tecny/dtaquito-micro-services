package com.dtaquito_backend.dtaquito_backend.rooms.application.internal.queryservices;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.services.RoomsQueryService;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomsQueryServiceImpl implements RoomsQueryService {

    private final RoomsRepository roomsRepository;

    public RoomsQueryServiceImpl(RoomsRepository roomsRepository) {
        this.roomsRepository = roomsRepository;
    }

    @Override
    public List<Rooms> getAllRooms() {
        return roomsRepository.findAll();
    }
}
