package com.dtaquito_micro_services.user_service.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(Long id, String username, String token) {

}
