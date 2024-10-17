package com.dtaquito_micro_services.sport_service.sportspaces.application.internal.commandservices;

import com.dtaquito_micro_services.sport_service.sportspaces.client.UserClient;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.exceptions.InvalidRoleTypeException;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.entities.User;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.model.events.SportSpacesCreatedEvent;
import com.dtaquito_micro_services.sport_service.sportspaces.domain.services.SportSpacesCommandService;
import com.dtaquito_micro_services.sport_service.sportspaces.infrastructure.persistance.jpa.SportRepository;
import com.dtaquito_micro_services.sport_service.sportspaces.infrastructure.persistance.jpa.SportSpacesRepository;
import com.dtaquito_micro_services.sport_service.config.impl.TokenCacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SportSpacesCommandServiceImpl implements SportSpacesCommandService {

    private final SportSpacesRepository sportSpacesRepository;
    private final SportRepository sportRepository;
    private final UserClient userClient;
    private final TokenCacheService tokenCacheService;

    public SportSpacesCommandServiceImpl(SportSpacesRepository sportSpacesRepository,
                                         SportRepository sportRepository, UserClient userClient,
                                         TokenCacheService tokenCacheService) {
        this.sportSpacesRepository = sportSpacesRepository;
        this.sportRepository = sportRepository;
        this.userClient = userClient;
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public Optional<SportSpaces> handle(Long userId, CreateSportSpacesCommand command) {
        String token = tokenCacheService.getToken("default");
        ResponseEntity<User> userResponse = userClient.getUserById(userId, token);
        if (userResponse.getStatusCode() != HttpStatus.OK || userResponse.getBody() == null) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userResponse.getBody();

        var sport = sportRepository.findById(command.sportId());
        if (!sportRepository.existsById(command.sportId())) {
            throw new InvalidRoleTypeException("Sport id: " + command.sportId() + " already exists");
        }
        var sportSpaces = new SportSpaces(command, user.getId(), sport.get());
        var createdSportSpaces = sportSpacesRepository.save(sportSpaces);
        return Optional.of(createdSportSpaces);
    }

    @Override
    public Optional<SportSpaces> handleUpdate(Long id, CreateSportSpacesCommand command) {
        var sportSpaces = sportSpacesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SportSpaces not found"));
        var sport = sportRepository.findById(command.sportId())
                .orElseThrow(() -> new IllegalArgumentException("Sport not found"));
        sportSpaces.update(command, sport);
        var updatedSportSpaces = sportSpacesRepository.save(sportSpaces);
        return Optional.of(updatedSportSpaces);
    }

    @Override
    public void handleDelete(Long id) {
        if (!sportSpacesRepository.existsById(id)) {
            throw new IllegalArgumentException("SportSpaces not found");
        }
        sportSpacesRepository.deleteById(id);
    }

    @Override
    public void handleSportSpacesCreatedEvent(SportSpacesCreatedEvent event) {
        System.out.println("SportSpacesCreatedEvent received for sportSpaces ID: " + event.getId());
    }
}