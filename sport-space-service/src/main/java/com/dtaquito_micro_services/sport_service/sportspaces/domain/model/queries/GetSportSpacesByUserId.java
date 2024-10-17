package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.queries;

public record GetSportSpacesByUserId(Long userId) {
    public GetSportSpacesByUserId {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
    }
}
