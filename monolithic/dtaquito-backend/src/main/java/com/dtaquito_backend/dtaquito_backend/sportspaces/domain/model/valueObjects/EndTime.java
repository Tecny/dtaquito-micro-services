package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects;

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
