package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.commands.SeedPlanTypesCommand;

public interface PlanCommandService {

    void handle(SeedPlanTypesCommand command);
}
