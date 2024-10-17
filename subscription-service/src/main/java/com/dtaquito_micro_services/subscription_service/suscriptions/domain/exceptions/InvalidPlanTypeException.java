package com.dtaquito_micro_services.subscription_service.suscriptions.domain.exceptions;

public class InvalidPlanTypeException extends RuntimeException{

    public InvalidPlanTypeException(String message) {
        super(message);
    }
}
