package com.dtaquito_micro_services.subscription_service.suscriptions.interfaces.rest.transform;

import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_micro_services.subscription_service.suscriptions.interfaces.rest.resources.SuscriptionsResource;

public class SuscriptionsResourceFromEntityAssembler {

    public static SuscriptionsResource toResourceFromEntity(Suscriptions entity) {
        return new SuscriptionsResource(entity.getId(), entity.getPlan().getId(), entity.getUserId(), entity.getPlan().getPlanType().name().toLowerCase());
    }
}
