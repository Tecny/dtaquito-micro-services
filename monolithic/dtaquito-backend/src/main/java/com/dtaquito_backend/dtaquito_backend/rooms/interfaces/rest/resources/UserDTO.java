package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.Views;
import com.fasterxml.jackson.annotation.JsonView;

public record UserDTO(
        @JsonView(Views.Public.class) Long id,
        @JsonView(Views.Public.class) String name,
        @JsonView(Views.Public.class) String email
) {
}