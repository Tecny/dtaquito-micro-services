package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.exceptions;

public class InvalidPlanTypeException extends RuntimeException{

    public InvalidPlanTypeException(String message) {
        super(message);
    }
}
