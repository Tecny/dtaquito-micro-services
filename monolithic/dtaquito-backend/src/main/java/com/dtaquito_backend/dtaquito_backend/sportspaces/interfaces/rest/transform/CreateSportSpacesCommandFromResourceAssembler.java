package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources.CreateSportSpacesResource;

public class CreateSportSpacesCommandFromResourceAssembler {

    public static CreateSportSpacesCommand toCommandFromResource(CreateSportSpacesResource resource) {
        return new CreateSportSpacesCommand(
                resource.name(),
                resource.sportId(),
                resource.imageUrl(),
                resource.price(),
                resource.district(),
                resource.description(),
                resource.userId(),
                resource.startTime(),
                resource.endTime(),
                resource.gamemode(),
                resource.amount()
        );
    }
}