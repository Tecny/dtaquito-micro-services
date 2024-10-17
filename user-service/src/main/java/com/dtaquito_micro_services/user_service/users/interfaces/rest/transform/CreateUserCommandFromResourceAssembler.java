package com.dtaquito_micro_services.user_service.users.interfaces.rest.transform;

import com.dtaquito_micro_services.user_service.users.domain.model.commands.CreateUserCommand;
import com.dtaquito_micro_services.user_service.users.interfaces.rest.resources.CreateUserResource;

public class CreateUserCommandFromResourceAssembler {

    public static CreateUserCommand toCommandFromResource(CreateUserResource resource) {
        return new CreateUserCommand(resource.name(), resource.email(), resource.password(), resource.roleId(), resource.bankAccount());
    }
}