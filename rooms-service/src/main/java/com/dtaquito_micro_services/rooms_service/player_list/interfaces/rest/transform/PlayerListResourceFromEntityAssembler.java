package com.dtaquito_micro_services.rooms_service.player_list.interfaces.rest.transform;

import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.player_list.interfaces.rest.resources.PlayerListDTO;

public class PlayerListResourceFromEntityAssembler {

    public static PlayerListDTO toResourceFromEntity(PlayerList playerList) {
        return new PlayerListDTO(
                playerList.getId(),
                playerList.getChatRoomId(),
                playerList.getRoomId(),
                playerList.getUserId()
        );
    }
}