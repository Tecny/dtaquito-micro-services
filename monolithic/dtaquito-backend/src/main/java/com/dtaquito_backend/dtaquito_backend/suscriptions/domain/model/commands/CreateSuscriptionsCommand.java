package com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.commands;

public record CreateSuscriptionsCommand(Long planId, Long userId, String token) {

}
