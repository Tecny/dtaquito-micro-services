package com.dtaquito_backend.dtaquito_backend.suscriptions.application.internal.eventhandlers;

import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.events.SuscriptionCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.queries.GetSuscriptionsByIdQuery;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsQueryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class SuscriptionCreatedEventHandler {

    private final SuscriptionsQueryService suscriptionQueryService;
    private final SuscriptionsCommandService suscriptionCommandService;

    public SuscriptionCreatedEventHandler(SuscriptionsQueryService suscriptionQueryService, SuscriptionsCommandService suscriptionCommandService) {
        this.suscriptionQueryService = suscriptionQueryService;
        this.suscriptionCommandService = suscriptionCommandService;
    }

    @EventListener(SuscriptionCreatedEvent.class)
    public void on(SuscriptionCreatedEvent event) {
        System.out.println("SuscriptionCreatedEvent received for suscription ID: " + event.getSuscriptionId());

        suscriptionCommandService.handleSuscriptionCreatedEvent(event);

        var getSuscriptionByIdQuery = new GetSuscriptionsByIdQuery(event.getSuscriptionId());

        var suscription = suscriptionQueryService.handle(getSuscriptionByIdQuery);

        if (suscription.isPresent()) {
            System.out.println("Suscription with ID " + event.getSuscriptionId() + " has been created.");
        } else {
            System.out.println("No suscription found with ID " + event.getSuscriptionId());
        }
    }
}
