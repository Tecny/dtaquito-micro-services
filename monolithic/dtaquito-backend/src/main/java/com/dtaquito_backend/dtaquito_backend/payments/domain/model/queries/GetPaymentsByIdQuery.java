package com.dtaquito_backend.dtaquito_backend.payments.domain.model.queries;

public record GetPaymentsByIdQuery(Long id) {

    public GetPaymentsByIdQuery {

        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }

}
