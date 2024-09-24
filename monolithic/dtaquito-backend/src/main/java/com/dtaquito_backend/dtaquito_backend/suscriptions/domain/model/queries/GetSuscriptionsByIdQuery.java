package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.queries;

public record GetSuscriptionsByIdQuery(Long id) {

    public GetSuscriptionsByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }
}
