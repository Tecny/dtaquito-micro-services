package com.dtaquito_micro_services.user_service.users.interfaces.rest.transform;

import com.dtaquito_micro_services.user_service.users.domain.model.aggregates.User;
import com.dtaquito_micro_services.user_service.users.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {

    public static UserResource toResourceFromEntity(User entity) {
        return new UserResource(entity.getId(), entity.getName(), entity.getEmail(), entity.getPassword(), entity.getRole().getId(), entity.getRole().getRoleType().name().toUpperCase(), entity.getBankAccount(), entity.getCredits());
    }
}
