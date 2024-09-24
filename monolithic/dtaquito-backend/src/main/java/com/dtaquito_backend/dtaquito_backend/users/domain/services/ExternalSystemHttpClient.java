package com.dtaquito_backend.dtaquito_backend.users.domain.services;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;

public interface ExternalSystemHttpClient {
    User getUserProfile(String userId);
    void updateUserProfile(User userProfile);
}