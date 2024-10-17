package com.dtaquito_micro_services.rooms_service.rooms.domain.services;


import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.SportSpaces;

public interface RoomsCommandService {

    void handleRooms();
    SportSpaces getSportSpace(Long sportSpaceId);
    void transferToCreator(Rooms room);
    void refundToUsers(Long playerListsId);
    void addPlayerToRoomAndChat(Long roomId, Long userId);
}
