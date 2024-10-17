package com.dtaquito_micro_services.user_service.iam.interfaces.rest.transform;


import com.dtaquito_micro_services.user_service.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_micro_services.user_service.iam.interfaces.rest.resources.SignUpResource;
import com.dtaquito_micro_services.user_service.users.domain.model.entities.Role;

import java.util.ArrayList;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var roles = resource.roles() != null ? resource.roles().stream().map(name -> Role.toRoleFromName(name)).toList() : new ArrayList<Role>();
        return new SignUpCommand(resource.name(), resource.email(), resource.password(), roles, resource.bankAccount());
    }
}
