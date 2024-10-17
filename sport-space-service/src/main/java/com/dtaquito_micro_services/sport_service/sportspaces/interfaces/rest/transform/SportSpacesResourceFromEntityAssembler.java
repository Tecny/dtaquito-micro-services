package com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.transform;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.resources.SportSpacesResource;

public class SportSpacesResourceFromEntityAssembler {

    public static SportSpacesResource toResourceFromEntity(SportSpaces entity) {
        return new SportSpacesResource(entity.getId(), entity.getName(), entity.getSport().getId(), entity.getSport().getSportType().name().toUpperCase(), entity.getImageUrl(), entity.getPrice(), entity.getDistrict() ,entity.getDescription(), entity.getUserId(), entity.getStartTime(), entity.getEndTime(), entity.getGamemode(), entity.getAmount());
    }
}