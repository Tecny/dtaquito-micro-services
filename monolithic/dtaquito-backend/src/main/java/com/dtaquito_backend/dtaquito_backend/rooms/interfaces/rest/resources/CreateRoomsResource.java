package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;

import java.util.Date;
import java.util.List;

public record CreateRoomsResource(

        Long CreatorId, String day, Date openingDate, String roomName, Long sportSpaceId,
        List<PlayerList> playerLists) {
}
