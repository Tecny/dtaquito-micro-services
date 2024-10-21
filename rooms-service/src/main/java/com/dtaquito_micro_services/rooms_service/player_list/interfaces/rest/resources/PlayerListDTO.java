package com.dtaquito_micro_services.rooms_service.player_list.interfaces.rest.resources;

import com.dtaquito_micro_services.rooms_service.rooms.domain.model.Views;
import com.fasterxml.jackson.annotation.JsonView;

public record PlayerListDTO(
        @JsonView(Views.Public.class) Long id
        , @JsonView(Views.Public.class) Long chatRoomId,
        @JsonView(Views.Public.class) Long roomId,
        @JsonView(Views.Public.class) Long userId

) {
}