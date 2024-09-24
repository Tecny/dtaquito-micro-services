package com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform;


import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.SignUpResource;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;

import java.util.ArrayList;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var roles = resource.roles() != null ? resource.roles().stream().map(name -> Role.toRoleFromName(name)).toList() : new ArrayList<Role>();
        return new SignUpCommand(resource.name(), resource.email(), resource.password(), roles, resource.bankAccount());
    }
}
