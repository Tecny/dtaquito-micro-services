package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.queries;

public record GetSportSpacesByIdQuery(Long id) {

    public GetSportSpacesByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }
}
