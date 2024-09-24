package com.dtaquito_backend.dtaquito_backend.users.domain.model.queries;

public record GetAllUserByNameQuery(String name) {

    public GetAllUserByNameQuery {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }
}
