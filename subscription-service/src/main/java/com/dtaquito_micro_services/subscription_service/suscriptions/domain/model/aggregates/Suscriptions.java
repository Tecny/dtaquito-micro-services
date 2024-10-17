package com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.aggregates;

import com.dtaquito_micro_services.subscription_service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities.Plan;
import com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Suscriptions extends AuditableAbstractAggregateRoot<Suscriptions> {

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    @Setter
    private Plan plan;

    private Long userId;

    public Suscriptions() {}

    public Suscriptions(Long userId, Plan plan) {
        this.userId = userId;
        this.plan = plan;
    }

    public void update(Plan plan) {
        this.plan = plan;
    }
}