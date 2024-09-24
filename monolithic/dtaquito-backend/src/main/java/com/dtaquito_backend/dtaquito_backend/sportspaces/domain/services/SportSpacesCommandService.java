package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.events.SportSpacesCreatedEvent;

import java.util.Optional;

public interface SportSpacesCommandService {

    Optional<SportSpaces> handle(Long id, CreateSportSpacesCommand command);
    Optional<SportSpaces> handleUpdate(Long id, CreateSportSpacesCommand command);
    void handleDelete(Long id);
    void handleSportSpacesCreatedEvent(SportSpacesCreatedEvent event);
}