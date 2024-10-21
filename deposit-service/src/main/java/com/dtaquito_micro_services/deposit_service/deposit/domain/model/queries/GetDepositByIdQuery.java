package com.dtaquito_micro_services.deposit_service.deposit.domain.model.queries;

public record GetDepositByIdQuery(Long id) {

    public GetDepositByIdQuery {
        if (id == null) {
            throw new IllegalArgumentException("Id is required");
        }
    }

    public Long getDepositId() {
        return id;
    }
}