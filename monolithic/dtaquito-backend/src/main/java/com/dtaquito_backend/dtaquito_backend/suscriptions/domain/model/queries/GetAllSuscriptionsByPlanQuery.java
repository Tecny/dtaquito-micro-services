package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.queries;

public record GetAllSuscriptionsByPlanQuery(String plan) {

    public GetAllSuscriptionsByPlanQuery {

        if (plan == null || plan.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
    }
}
