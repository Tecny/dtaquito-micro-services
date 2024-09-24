package com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;

import java.util.List;

public record SignUpCommand(String name, String email, String password, List<Role> roles, String bankAccount) {
}
