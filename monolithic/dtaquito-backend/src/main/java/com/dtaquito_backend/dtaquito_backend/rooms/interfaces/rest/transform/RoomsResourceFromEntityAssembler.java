package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources.RoomsResource;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources.SportSpaceDTO;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources.UserDTO;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources.PlayerListUserDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class RoomsResourceFromEntityAssembler {

    public static RoomsResource toResourceFromEntity(Rooms room) {
        UserDTO creator = new UserDTO(
                room.getCreator().getId(),
                room.getCreator().getName(),
                room.getCreator().getEmail()
        );

        SportSpaceDTO sportSpace = new SportSpaceDTO(
                room.getSportSpace().getId(),
                room.getSportSpace().getName(),
                room.getSportSpace().getSport().getId(),
                room.getSportSpace().getImageUrl(),
                new BigDecimal(room.getSportSpace().getPrice()),
                room.getSportSpace().getDistrict(),
                room.getSportSpace().getDescription(),
                room.getSportSpace().getStartTime(),
                room.getSportSpace().getEndTime(),
                room.getSportSpace().getGamemode().toString(),
                room.getSportSpace().getAmount()
        );

        List<PlayerListUserDTO> playerListUserDTOs = room.getPlayerLists().stream()
                .map(playerList -> new PlayerListUserDTO(playerList.getUser().getId()))
                .collect(Collectors.toList());

        return new RoomsResource(
                room.getId(),
                creator,
                room.getDay(),
                room.getOpeningDate(),
                room.getRoomName(),
                sportSpace,
                playerListUserDTOs,
                room.getAccumulatedAmount()
        );
    }
}