package com.dtaquito_micro_services.rooms_service.rooms.domain.services;

import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;

import java.util.List;

public interface RoomsQueryService {

    List<Rooms> getAllRooms();
}
