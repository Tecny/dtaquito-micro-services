package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public final class SuscriptionCreatedEvent extends ApplicationEvent {

    private final Long suscriptionId;

    public SuscriptionCreatedEvent(Object source, Long suscriptionId) {
        super(source);
        this.suscriptionId = suscriptionId;
    }
}
