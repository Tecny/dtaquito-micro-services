package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects;

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
