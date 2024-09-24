package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries;

public record GetSportSpacesByUserId(Long userId) {
    public GetSportSpacesByUserId {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
    }
}
