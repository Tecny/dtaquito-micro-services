package com.dtaquito_backend.dtaquito_backend.suscriptions.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.payments.application.internal.commandservices.PayPalPaymentServiceImpl;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.events.SuscriptionCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.services.SuscriptionsCommandService;
import com.dtaquito_backend.dtaquito_backend.suscriptions.infrastructure.persistance.jpa.PlanRepository;
import com.dtaquito_backend.dtaquito_backend.suscriptions.infrastructure.persistance.jpa.SuscriptionsRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuscriptionsCommandServiceImpl implements SuscriptionsCommandService {

    private final SuscriptionsRepository suscriptionsRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SuscriptionsCommandServiceImpl(SuscriptionsRepository suscriptionsRepository, UserRepository userRepository, PlanRepository planRepository, ApplicationEventPublisher applicationEventPublisher, PayPalPaymentServiceImpl payPalPaymentServiceImpl) {
        this.suscriptionsRepository = suscriptionsRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Optional<Suscriptions> handle(CreateSuscriptionsCommand command) {
        System.out.println("Token: " + command.token());

        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Plan plan = planRepository.findById(command.planId())
                .orElseGet(() -> planRepository.findByPlanType(PlanTypes.free)
                        .orElseThrow(() -> new IllegalArgumentException("Free plan not found")));

        // Check if the user already has subscriptions
        List<Suscriptions> existingSuscriptions = suscriptionsRepository.findByUserId(user.getId());

        if (!existingSuscriptions.isEmpty()) {
            // Update the existing subscriptions
            for (Suscriptions suscription : existingSuscriptions) {
                suscription.setPlan(plan);
                suscriptionsRepository.save(suscription);
            }
            return Optional.of(existingSuscriptions.get(0));
        }

        // Create a new subscription
        var suscriptions = new Suscriptions(plan, user);
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