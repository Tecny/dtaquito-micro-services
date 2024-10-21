package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.entities;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects.RoleTypes;
import lombok.Data;

@Data
public class Role {

    private Long id;
    private RoleTypes roleType;
}
