package com.dtaquito_micro_services.user_service.users.interfaces.rest.resources;

public record CreateUserResource(
        String name,
        String email,
        String password,
        Long roleId,
        String bankAccount) { }
