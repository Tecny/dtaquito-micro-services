package com.dtaquito_micro_services.user_service.iam.domain.services;

import com.dtaquito_micro_services.user_service.iam.domain.model.queries.GetAllRolesQuery;
import com.dtaquito_micro_services.user_service.iam.domain.model.queries.GetRoleByNameQuery;
import com.dtaquito_micro_services.user_service.users.domain.model.entities.Role;

import java.util.List;
import java.util.Optional;

public interface RoleQueryService {

    List<Role> handle(GetAllRolesQuery query);
    Optional<Role> handle(GetRoleByNameQuery query);
}
