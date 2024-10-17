package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Image(String image) {

    public Image() {
        this("");
    }

    public Image {
        if (image.isBlank()) {
            throw new IllegalArgumentException("Image cannot be blank");
        }
    }
}
