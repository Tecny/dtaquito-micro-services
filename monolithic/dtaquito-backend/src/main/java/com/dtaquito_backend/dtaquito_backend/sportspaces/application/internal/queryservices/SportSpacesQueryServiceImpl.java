package com.dtaquito_backend.dtaquito_backend.sportspaces.application.internal.queryservices;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetAllSportSpacesByNameQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByIdQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByUserId;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesQueryService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportSpacesRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SportSpacesQueryServiceImpl implements SportSpacesQueryService {

    private final SportSpacesRepository sportSpacesRepository;

    public SportSpacesQueryServiceImpl(SportSpacesRepository sportSpacesRepository) {
        this.sportSpacesRepository = sportSpacesRepository;
    }

    @Override
    public List<SportSpaces> handle(GetAllSportSpacesByNameQuery query) {
        return sportSpacesRepository.findAllByName(query.name());
    }

    @Override
    public Optional<SportSpaces> handle(GetSportSpacesByIdQuery query) {
        return sportSpacesRepository.findById(query.id());
    }

    @Override
    public List<SportSpaces> getAllSportSpaces() {
        return sportSpacesRepository.findAll();
    }

    @Override
    public List<SportSpaces> handle(GetSportSpacesByUserId query) {
        return sportSpacesRepository.findByUserId(query.userId());
    }
}
