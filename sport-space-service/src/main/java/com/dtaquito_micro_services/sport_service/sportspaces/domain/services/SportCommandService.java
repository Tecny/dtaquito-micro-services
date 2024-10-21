package com.dtaquito_micro_services.sport_service.sportspaces.domain.services;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.commands.SeedSportTypeCommand;

public interface SportCommandService {
    void handle(SeedSportTypeCommand command);
}
