package com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities;

import com.dtaquito_micro_services.rooms_service.rooms.domain.model.valueobjects.GameMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SportSpaces {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("sport")
    private Long sportId;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("price")
    private Long price;

    @JsonProperty("district")
    private String district;

    @JsonProperty("description")
    private String description;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("startTime")
    private String startTime;

    @JsonProperty("endTime")
    private String endTime;

    @JsonProperty("gamemode")
    private GameMode gamemode;

    @JsonProperty("amount")
    private Integer amount;
}
