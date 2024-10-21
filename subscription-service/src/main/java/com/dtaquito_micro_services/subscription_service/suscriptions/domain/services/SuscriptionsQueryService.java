package com.dtaquito_micro_services.subscription_service.suscriptions.domain.services;

import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.queries.GetAllSuscriptionsByPlanQuery;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.queries.GetSuscriptionsByIdQuery;

import java.util.List;
import java.util.Optional;

public interface SuscriptionsQueryService {

    List<Suscriptions> handle(GetAllSuscriptionsByPlanQuery query);
    Optional<Suscriptions> handle(GetSuscriptionsByIdQuery query);
    List<Suscriptions> getAllSuscriptions();
    Optional<Suscriptions> getSuscriptionById(Long id);

    Optional<Suscriptions> getSubscriptionByUserId(Long userId);
}
