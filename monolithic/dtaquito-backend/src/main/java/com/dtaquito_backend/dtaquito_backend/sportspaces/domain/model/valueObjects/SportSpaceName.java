package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record SportSpaceName(String sportSpaceName) {

    public SportSpaceName() {
        this("");
    }

    public SportSpaceName {
        if (sportSpaceName.isBlank()) {
            throw new IllegalArgumentException("Sport space name cannot be blank");
        }
    }
}
