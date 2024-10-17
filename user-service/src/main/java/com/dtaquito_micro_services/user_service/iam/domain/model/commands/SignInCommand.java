package com.dtaquito_micro_services.user_service.iam.domain.model.commands;

public record SignInCommand(String email, String password) {
}
