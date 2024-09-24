package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services;

import java.util.Optional;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.events.SuscriptionCreatedEvent;

public interface SuscriptionsCommandService {

    Optional<Suscriptions> handle(CreateSuscriptionsCommand command);

    Optional<Suscriptions> updateSuscription(Suscriptions suscription);

    void handleSuscriptionCreatedEvent(SuscriptionCreatedEvent event);
}