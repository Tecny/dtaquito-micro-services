package com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources;

import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.Views;
import com.fasterxml.jackson.annotation.JsonView;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record RoomsResource(
        @JsonView(Views.Public.class) Long id,
        @JsonView(Views.Public.class) UserDTO creator,
        @JsonView(Views.Public.class) String day,
        @JsonView(Views.Public.class) Date openingDate,
        @JsonView(Views.Public.class) String roomName,
        @JsonView(Views.Public.class) SportSpaceDTO sportSpace,
        @JsonView(Views.Public.class) List<PlayerListUserDTO> playerLists,
        @JsonView(Views.Public.class) BigDecimal accumulatedAmount
) {
}