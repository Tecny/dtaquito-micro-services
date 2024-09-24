package com.dtaquito_backend.dtaquito_backend.rooms.domain.model.queries;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;

import java.util.List;

public interface GetAllRoomsByIdQuery {

    List<Rooms> getAllRoomsById(Long id);
}
