package com.dtaquito_micro_services.user_service.iam.domain.model.commands;

import com.dtaquito_micro_services.user_service.users.domain.model.entities.Role;

import java.util.List;

public record SignUpCommand(String name, String email, String password, List<Role> roles, String bankAccount) {
}
