package com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform;


import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.RoleResource;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;

public class RoleResourceFromEntityAssembler {
    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}
