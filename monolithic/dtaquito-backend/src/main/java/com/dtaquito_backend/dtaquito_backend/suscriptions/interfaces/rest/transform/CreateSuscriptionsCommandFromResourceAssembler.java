package com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.resources.CreateSuscriptionsResource;

public class CreateSuscriptionsCommandFromResourceAssembler {

    public static CreateSuscriptionsCommand toCommandFromResource(CreateSuscriptionsResource resource) {
        return new CreateSuscriptionsCommand(resource.planId(), resource.userId(), resource.token());
    }
}