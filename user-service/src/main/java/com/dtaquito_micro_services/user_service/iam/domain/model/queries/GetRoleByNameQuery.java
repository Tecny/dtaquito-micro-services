package com.dtaquito_micro_services.user_service.iam.domain.model.queries;


import com.dtaquito_micro_services.user_service.users.domain.model.valueObjects.RoleTypes;

public record GetRoleByNameQuery(RoleTypes name) {
}
