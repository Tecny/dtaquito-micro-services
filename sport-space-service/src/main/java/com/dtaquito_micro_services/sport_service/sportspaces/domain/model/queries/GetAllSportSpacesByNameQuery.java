package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.queries;

public record GetAllSportSpacesByNameQuery(String name) {

    public GetAllSportSpacesByNameQuery {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }
}
