package com.dtaquito_micro_services.user_service.iam.interfaces.rest.transform;


import com.dtaquito_micro_services.user_service.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.dtaquito_micro_services.user_service.users.domain.model.aggregates.User;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(user.getId(), user.getName(), token);
    }
}
