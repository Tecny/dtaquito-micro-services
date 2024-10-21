package com.dtaquito_micro_services.sport_service.sportspaces.domain.services;

import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.queries.GetAllSportSpacesByNameQuery;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.queries.GetSportSpacesByIdQuery;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.queries.GetSportSpacesByUserId;

import java.util.List;
import java.util.Optional;

public interface SportSpacesQueryService {

    List<SportSpaces>handle(GetAllSportSpacesByNameQuery query);
    Optional<SportSpaces> handle(GetSportSpacesByIdQuery query);
    List<SportSpaces> getAllSportSpaces();

    List<SportSpaces> handle(GetSportSpacesByUserId query);
}
