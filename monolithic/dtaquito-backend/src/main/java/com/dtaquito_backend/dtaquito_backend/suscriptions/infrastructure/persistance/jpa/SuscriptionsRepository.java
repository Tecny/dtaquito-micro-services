package com.dtaquito_backend.dtaquito_backend.suscriptions.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscriptionsRepository extends JpaRepository<Suscriptions, Long> {

    List<Suscriptions> findAllByPlanId(Long planId);
    List<Suscriptions> findByUserId(Long userId);

}
