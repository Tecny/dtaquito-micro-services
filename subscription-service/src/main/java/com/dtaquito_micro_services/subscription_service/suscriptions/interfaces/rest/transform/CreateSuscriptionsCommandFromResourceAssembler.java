package com.dtaquito_micro_services.subscription_service.suscriptions.interfaces.rest.transform;

import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_micro_services.subscription_service.suscriptions.interfaces.rest.resources.CreateSuscriptionsResource;

public class CreateSuscriptionsCommandFromResourceAssembler {

    public static CreateSuscriptionsCommand toCommandFromResource(CreateSuscriptionsResource resource) {
        return new CreateSuscriptionsCommand(resource.planId(), resource.userId());
    }
}