package com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources;

import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;

import java.util.Date;
import java.util.List;

public record CreateRoomsResource(

        Long CreatorId, String day, Date openingDate, String roomName, Long sportSpaceId,
        List<PlayerList> playerLists) {
}
