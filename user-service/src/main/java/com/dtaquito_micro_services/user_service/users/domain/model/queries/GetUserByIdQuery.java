package com.dtaquito_micro_services.user_service.users.domain.model.queries;

public record GetUserByIdQuery(Long id) {

    public GetUserByIdQuery {

        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }
}
