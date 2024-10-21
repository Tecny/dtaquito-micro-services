package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SportSpacesCreatedEvent extends ApplicationEvent {

    private final Long id;

    public SportSpacesCreatedEvent(Object source, Long id) {
        super(source);
        this.id = id;
    }
}
