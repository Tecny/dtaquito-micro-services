package com.dtaquito_backend.dtaquito_backend.sportspaces.interfaces.rest.resources;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.GameMode;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;

public record SportSpacesResource(Long id, String name, Long sportId, String sportType, String imageUrl, Long price, String district, String description, User user, String StartTime, String endTime,
                                   GameMode gamemode, Integer amount) {}