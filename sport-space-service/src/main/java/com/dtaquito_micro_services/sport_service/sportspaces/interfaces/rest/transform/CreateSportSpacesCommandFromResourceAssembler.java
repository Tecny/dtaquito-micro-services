package com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.transform;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.resources.CreateSportSpacesResource;

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