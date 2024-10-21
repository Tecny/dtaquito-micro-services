package com.dtaquito_micro_services.subscription_service.suscriptions.domain.services;

import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.commands.SeedPlanTypesCommand;

public interface PlanCommandService {

    void handle(SeedPlanTypesCommand command);
}
