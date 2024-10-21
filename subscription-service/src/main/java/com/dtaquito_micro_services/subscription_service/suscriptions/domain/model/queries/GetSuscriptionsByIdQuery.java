package com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.queries;

public record GetSuscriptionsByIdQuery(Long id) {

    public GetSuscriptionsByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }
}
