package com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.resources;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.entities.User;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects.District;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.valueObjects.GameMode;

public record SportSpacesResource(Long id, String name, Long sportId, String sportType, String imageUrl, Double price, District district, String description, Long userId, String StartTime, String endTime,
                                  GameMode gamemode, Integer amount) {}