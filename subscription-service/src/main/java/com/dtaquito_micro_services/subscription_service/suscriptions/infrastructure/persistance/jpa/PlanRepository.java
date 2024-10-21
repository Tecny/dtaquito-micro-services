package com.dtaquito_micro_services.subscription_service.suscriptions.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities.Plan;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.valueObjects.PlanTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long>{

    boolean existsByPlanType(PlanTypes planType);
    Optional<Plan> findByPlanType(PlanTypes planType);

}