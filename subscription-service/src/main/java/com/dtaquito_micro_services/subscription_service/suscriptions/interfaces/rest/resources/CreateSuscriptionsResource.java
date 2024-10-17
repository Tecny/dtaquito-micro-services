package com.dtaquito_micro_services.subscription_service.suscriptions.interfaces.rest.resources;

public record CreateSuscriptionsResource(Long planId, Long userId, String token) {}
