package com.dtaquito_backend.dtaquito_backend.rooms.domain.model.commands;

import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;

import java.util.Date;
import java.util.List;

public record CreateRoomCommand(
        Long creatorId,
        String day,
        Date openingDate,
        String roomName,
        Long sportSpaceId,
        List<PlayerList> playerLists
) {
}