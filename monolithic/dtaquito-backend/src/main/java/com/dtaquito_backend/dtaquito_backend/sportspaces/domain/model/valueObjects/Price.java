package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Price(Long price) {

    public Price() {
        this(0L);
    }

    public Price {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}
