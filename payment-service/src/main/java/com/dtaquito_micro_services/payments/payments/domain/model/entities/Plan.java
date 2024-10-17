package com.dtaquito_micro_services.payments.payments.domain.model.entities;

import com.dtaquito_micro_services.payments.payments.domain.model.valueobjects.PlanTypes;
import lombok.Data;

@Data
public class Plan {

    private Long id;
    private PlanTypes planType;
}
