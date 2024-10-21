package com.dtaquito_micro_services.sport_service.sportspaces.domain.exceptions;

public class InvalidRoleTypeException extends RuntimeException {

    public InvalidRoleTypeException(String message) {
        super(message);
    }
}
