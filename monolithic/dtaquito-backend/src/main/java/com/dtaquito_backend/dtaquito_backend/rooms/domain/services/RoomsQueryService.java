package com.dtaquito_backend.dtaquito_backend.rooms.domain.services;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;

import java.util.List;

public interface RoomsQueryService {

    List<Rooms> getAllRooms();
}
