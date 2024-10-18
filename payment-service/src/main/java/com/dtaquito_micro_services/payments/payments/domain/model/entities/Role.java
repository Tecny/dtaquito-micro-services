package com.dtaquito_micro_services.payments.payments.domain.model.entities;

import com.dtaquito_micro_services.payments.payments.domain.model.valueobjects.RoleTypes;
import lombok.Data;

@Data
public class Role {

    private Long id;
    private RoleTypes roleType;
}
