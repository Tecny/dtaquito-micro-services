package com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.queries;

public record GetAllSuscriptionsByPlanQuery(String plan) {

    public GetAllSuscriptionsByPlanQuery {

        if (plan == null || plan.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
    }
}
