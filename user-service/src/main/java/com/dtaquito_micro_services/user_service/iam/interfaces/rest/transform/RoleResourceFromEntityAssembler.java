package com.dtaquito_micro_services.user_service.iam.interfaces.rest.transform;


import com.dtaquito_micro_services.user_service.iam.interfaces.rest.resources.RoleResource;
import com.dtaquito_micro_services.user_service.users.domain.model.entities.Role;

public class RoleResourceFromEntityAssembler {
    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}
