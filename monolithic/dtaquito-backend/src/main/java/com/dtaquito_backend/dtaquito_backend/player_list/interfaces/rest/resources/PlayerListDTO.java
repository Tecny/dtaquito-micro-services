package com.dtaquito_backend.dtaquito_backend.player_list.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.Views;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.fasterxml.jackson.annotation.JsonView;

public record PlayerListDTO(
        @JsonView(Views.Public.class) Long id
        , @JsonView(Views.Public.class) Long chatRoomId,
        @JsonView(Views.Public.class) Long roomId,
        @JsonView(Views.Public.class) Long userId

) {
}