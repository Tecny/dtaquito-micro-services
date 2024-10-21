package com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.transform;

import com.dtaquito_micro_services.rooms_service.rooms.domain.model.commands.CreateRoomCommand;
import com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources.CreateRoomsResource;

public class CreateRoomsCommandFromResourceAssembler {

    public static CreateRoomCommand toCommandFromResource(CreateRoomsResource resource) {
        return new CreateRoomCommand(
                resource.CreatorId(),
                resource.day(),
                resource.openingDate(),
                resource.roomName(),
                resource.sportSpaceId(),
                resource.playerLists()
        );
    }
}
