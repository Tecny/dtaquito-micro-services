package com.dtaquito_micro_services.subscription_service.suscriptions.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.aggregates.Suscriptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuscriptionsRepository extends JpaRepository<Suscriptions, Long> {

    List<Suscriptions> findAllByPlanId(Long planId);
    List<Suscriptions> findByUserId(Long userId);

}
