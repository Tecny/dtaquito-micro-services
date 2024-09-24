package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetAllSportSpacesByNameQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByIdQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByUserId;
import com.dtaquito_backend.dtaquito_backend.suscriptions.domain.model.aggregates.Suscriptions;

import java.util.List;
import java.util.Optional;

public interface SportSpacesQueryService {

    List<SportSpaces>handle(GetAllSportSpacesByNameQuery query);
    Optional<SportSpaces> handle(GetSportSpacesByIdQuery query);
    List<SportSpaces> getAllSportSpaces();

    List<SportSpaces> handle(GetSportSpacesByUserId query);
}
