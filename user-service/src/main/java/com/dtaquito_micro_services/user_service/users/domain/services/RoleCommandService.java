package com.dtaquito_micro_services.user_service.users.domain.services;

import com.dtaquito_micro_services.user_service.users.domain.model.commands.SeedRoleTypeCommand;

public interface RoleCommandService {

    void handle(SeedRoleTypeCommand command);
}
