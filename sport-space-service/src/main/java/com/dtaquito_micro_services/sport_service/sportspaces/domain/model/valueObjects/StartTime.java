package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record StartTime(String startTime) {

    public StartTime() {
        this("");
    }

    public StartTime {
        if (startTime.isBlank()) {
            throw new IllegalArgumentException("Start time cannot be blank");
        }
    }
}
