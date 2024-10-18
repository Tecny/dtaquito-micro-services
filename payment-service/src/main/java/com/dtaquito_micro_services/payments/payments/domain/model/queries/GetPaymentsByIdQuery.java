package com.dtaquito_micro_services.payments.payments.domain.model.queries;

public record GetPaymentsByIdQuery(Long id) {

    public GetPaymentsByIdQuery {

        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }

}
