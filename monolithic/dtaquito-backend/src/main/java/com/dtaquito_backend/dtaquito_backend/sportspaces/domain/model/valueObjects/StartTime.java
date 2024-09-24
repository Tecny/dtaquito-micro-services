package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects;

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
