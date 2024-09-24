package com.dtaquito_backend.dtaquito_backend.suscriptions.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.valueObjects.PlanTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long>{

    boolean existsByPlanType(PlanTypes planType);
    Optional<Plan> findByPlanType(PlanTypes planType);

}