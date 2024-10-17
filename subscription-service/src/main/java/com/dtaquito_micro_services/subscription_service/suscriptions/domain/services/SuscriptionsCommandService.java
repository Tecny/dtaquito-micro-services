package com.dtaquito_micro_services.subscription_service.suscriptions.domain.services;

import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.events.SuscriptionCreatedEvent;

import java.util.Optional;

public interface SuscriptionsCommandService {

    Optional<Suscriptions> handle(CreateSuscriptionsCommand command);

    Optional<Suscriptions> updateSuscription(Suscriptions suscription);

    void handleSuscriptionCreatedEvent(SuscriptionCreatedEvent event);
}