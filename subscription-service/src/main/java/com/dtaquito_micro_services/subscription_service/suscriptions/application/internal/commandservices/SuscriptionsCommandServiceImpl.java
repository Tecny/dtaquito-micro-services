package com.dtaquito_micro_services.subscription_service.suscriptions.application.internal.commandservices;

import com.dtaquito_micro_services.subscription_service.suscriptions.client.PaymentClient;
import com.dtaquito_micro_services.subscription_service.suscriptions.client.UserClient;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities.Plan;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities.User;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.events.SuscriptionCreatedEvent;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.services.SuscriptionsCommandService;
import com.dtaquito_micro_services.subscription_service.suscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_micro_services.subscription_service.suscriptions.infrastructure.persistance.jpa.SuscriptionsRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuscriptionsCommandServiceImpl implements SuscriptionsCommandService {

    private final SuscriptionsRepository suscriptionsRepository;
    private final PlanRepository planRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PaymentClient paymentClient;
    private final UserClient userClient;

    public SuscriptionsCommandServiceImpl(SuscriptionsRepository suscriptionsRepository, PlanRepository planRepository, ApplicationEventPublisher applicationEventPublisher, PaymentClient paymentClient, UserClient userClient) {
        this.suscriptionsRepository = suscriptionsRepository;
        this.planRepository = planRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.paymentClient = paymentClient;
        this.userClient = userClient;
    }

    @Override
    public Optional<Suscriptions> handle(CreateSuscriptionsCommand command) {
        // Directly use the userId from the command
        Long userId = command.userId();

        Plan plan = planRepository.findById(command.planId())
                .orElseGet(() -> planRepository.findByPlanType(PlanTypes.free)
                        .orElseThrow(() -> new IllegalArgumentException("Free plan not found")));

        // Check if the user already has subscriptions
        List<Suscriptions> existingSuscriptions = suscriptionsRepository.findByUserId(userId);

        if (!existingSuscriptions.isEmpty()) {
            // Update the existing subscriptions
            for (Suscriptions suscription : existingSuscriptions) {
                suscription.setPlan(plan);
                suscriptionsRepository.save(suscription);
            }
            return Optional.of(existingSuscriptions.get(0));
        }

        // Create a new subscription
        var suscriptions = new Suscriptions(userId, plan);
        var createdSuscriptions = suscriptionsRepository.save(suscriptions);
        SuscriptionCreatedEvent event = new SuscriptionCreatedEvent(this, createdSuscriptions.getId());
        applicationEventPublisher.publishEvent(event);
        return Optional.of(createdSuscriptions);
    }

    @Override
    public Optional<Suscriptions> updateSuscription(Suscriptions suscription) {
        var updatedSuscription = suscriptionsRepository.save(suscription);
        return Optional.of(updatedSuscription);
    }

    @Override
    public void handleSuscriptionCreatedEvent(SuscriptionCreatedEvent event) {
        System.out.println("SuscriptionDeletedEvent received for suscription ID: " + event.getSuscriptionId());
    }
}