package com.dtaquito_backend.dtaquito_backend.suscriptions.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;

public record SuscriptionsResource(Long id, Long planId, User user, String planType) {
}
