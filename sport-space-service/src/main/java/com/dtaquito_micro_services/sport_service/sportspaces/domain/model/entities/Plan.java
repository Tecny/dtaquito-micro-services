package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.entities;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects.PlanTypes;
import lombok.Data;

@Data
public class Plan {

    private Long id;
    private PlanTypes planType;
}
