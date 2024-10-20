package com.dtaquito_micro_services.subscription_service.suscriptions.application.internal.commandservices;

import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.commands.SeedPlanTypesCommand;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities.Plan;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.services.PlanCommandService;
import com.dtaquito_micro_services.subscription_service.suscriptions.infrastructure.persistance.jpa.PlanRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PlanCommandServiceImpl implements PlanCommandService {

    private final PlanRepository planRepository;

    public PlanCommandServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @PostConstruct
    public void init() {
        handle(new SeedPlanTypesCommand());
    }

    @Override
    public void handle(SeedPlanTypesCommand command) {
        Arrays.stream(PlanTypes.values()).forEach(planType ->{
            if(!planRepository.existsByPlanType(planType)){
                planRepository.save(new Plan(planType));
            }
        });
    }
}
