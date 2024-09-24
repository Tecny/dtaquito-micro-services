package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.queries.GetAllSuscriptionsByPlanQuery;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.queries.GetSuscriptionsByIdQuery;

import java.util.List;
import java.util.Optional;

public interface SuscriptionsQueryService {

    List<Suscriptions> handle(GetAllSuscriptionsByPlanQuery query);
    Optional<Suscriptions> handle(GetSuscriptionsByIdQuery query);
    List<Suscriptions> getAllSuscriptions();
    Optional<Suscriptions> getSuscriptionById(Long id);

    Optional<Suscriptions> getSubscriptionByUserId(Long userId);
}
