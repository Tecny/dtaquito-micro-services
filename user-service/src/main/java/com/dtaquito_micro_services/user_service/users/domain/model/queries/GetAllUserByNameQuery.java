package com.dtaquito_micro_services.user_service.users.domain.model.queries;

public record GetAllUserByNameQuery(String name) {

    public GetAllUserByNameQuery {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }
}
