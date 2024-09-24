package com.dtaquito_backend.dtaquito_backend.payments.domain.model.commands;

public record CreatePaymentsCommand(Long userId, String paypalUserId, String paypalEmail, Long amount, String currency, String description) {

    public CreatePaymentsCommand {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (paypalUserId == null) {
            throw new IllegalArgumentException("PayPal User ID cannot be null");
        }
        if (paypalEmail == null) {
            throw new IllegalArgumentException("PayPal Email cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
    }
}