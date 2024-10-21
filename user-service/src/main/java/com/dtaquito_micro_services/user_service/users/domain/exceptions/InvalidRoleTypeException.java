package com.dtaquito_micro_services.user_service.users.domain.exceptions;

public class InvalidRoleTypeException extends RuntimeException {

    public InvalidRoleTypeException(String message) {
        super(message);
    }
}
