package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record EndTime(String endTime) {

    public EndTime() {
        this("");
    }

    public EndTime {
        if (endTime.isBlank()) {
            throw new IllegalArgumentException("End time cannot be blank");
        }
    }
}
