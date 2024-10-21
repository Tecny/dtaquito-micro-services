package com.dtaquito_micro_services.payments.payments.domain.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("roleId")
    private Long roleId;

    @JsonProperty("roleType")
    private String roleType;

    @JsonProperty("allowedSportSpaces")
    private int allowedSportSpaces = 0;

    @JsonProperty("plan")
    private String plan;

    @JsonProperty("bankAccount")
    private String bankAccount;

    @JsonProperty("credits")
    private BigDecimal credits = BigDecimal.ZERO;
}