package com.dtaquito_micro_services.subscription_service.suscriptions.domain.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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


    public void setAllowedSportspacesBasedOnPlan() {
        switch (this.plan) {
            case "free":
                this.allowedSportSpaces = 0;
                break;
            case "bronce":
                this.allowedSportSpaces = 1;
                break;
            case "plata":
                this.allowedSportSpaces = 2;
                break;
            case "oro":
                this.allowedSportSpaces = 3;
                break;
            default:
                throw new IllegalArgumentException("Unknown plan type: " + this.plan);
        }
    }

    public void setAllowedSportspaces(int allowedSportspaces) {
        this.allowedSportSpaces = allowedSportspaces;
    }
}