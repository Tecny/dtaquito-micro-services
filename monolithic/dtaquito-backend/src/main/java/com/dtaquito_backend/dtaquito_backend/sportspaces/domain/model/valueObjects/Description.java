package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Description(String description) {

    public Description () {
        this("");
    }
    public Description {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        if (description.length() > 255) {
            throw new IllegalArgumentException("Description cannot be longer than 255 characters");
        }
    }
}
