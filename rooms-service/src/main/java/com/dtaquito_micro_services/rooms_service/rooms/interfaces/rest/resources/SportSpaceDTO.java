// SportSpaceDTO.java
package com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources;

import com.dtaquito_micro_services.rooms_service.rooms.domain.model.Views;
import com.fasterxml.jackson.annotation.JsonView;

import java.math.BigDecimal;

public record SportSpaceDTO(
        @JsonView(Views.Public.class) Long id,
        @JsonView(Views.Public.class) String name,
        @JsonView(Views.Public.class) Long sportId,
        @JsonView(Views.Public.class) String imageUrl,
        @JsonView(Views.Public.class) BigDecimal price,
        @JsonView(Views.Public.class) String district,
        @JsonView(Views.Public.class) String description,
        @JsonView(Views.Public.class) String startTime,
        @JsonView(Views.Public.class) String endTime,
        @JsonView(Views.Public.class) String gamemode,
        @JsonView(Views.Public.class) Integer amount
) {
}