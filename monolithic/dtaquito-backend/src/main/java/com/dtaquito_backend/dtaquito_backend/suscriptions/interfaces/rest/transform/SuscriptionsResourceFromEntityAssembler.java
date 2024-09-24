package com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.transform;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.resources.SuscriptionsResource;

public class SuscriptionsResourceFromEntityAssembler {

    public static SuscriptionsResource toResourceFromEntity(Suscriptions entity) {
        return new SuscriptionsResource(entity.getId(), entity.getPlan().getId(), entity.getUser(), entity.getPlan().getPlanType().name().toLowerCase());
    }
}
