package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.commands.CreateSuscriptionsCommand;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.entities.Plan;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.valueObjects.PlanTypes;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
public class Suscriptions extends AuditableAbstractAggregateRoot<Suscriptions> {

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    @Setter
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_Id_unique"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    public Suscriptions() {}

    public Suscriptions(Plan plan, User user) {
        this.plan = plan;
        this.user = user;
    }

    public void update(Plan plan) {
        this.plan = plan;
    }
}