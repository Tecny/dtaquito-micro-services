package com.dtaquito_micro_services.rooms_service.rooms.domain.model.queries;

import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;

import java.util.List;

public interface GetAllRoomsByIdQuery {

    List<Rooms> getAllRoomsById(Long id);
}
