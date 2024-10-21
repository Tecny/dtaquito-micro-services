package com.dtaquito_micro_services.payments.payments.domain.model.entities;

import lombok.Data;

@Data
public class Suscriptions {

    private Long id;
    private Plan plan;
    private User user;
}
