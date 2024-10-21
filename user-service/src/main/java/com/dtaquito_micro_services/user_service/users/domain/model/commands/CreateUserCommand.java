package com.dtaquito_micro_services.user_service.users.domain.model.commands;

public record CreateUserCommand(String name, String email, String password, Long roleId, String bankAccount) { }