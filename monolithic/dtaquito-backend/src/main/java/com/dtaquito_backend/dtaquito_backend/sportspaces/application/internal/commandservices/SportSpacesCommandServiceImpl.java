package com.dtaquito_backend.dtaquito_backend.sportspaces.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.events.SportSpacesCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesCommandService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportRepository;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportSpacesRepository;
import com.dtaquito_backend.dtaquito_backend.users.domain.exceptions.InvalidRoleTypeException;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SportSpacesCommandServiceImpl implements SportSpacesCommandService {

    private final SportSpacesRepository sportSpacesRepository;
    private final UserRepository userRepository;
    private final SportRepository sportRepository;

    public SportSpacesCommandServiceImpl(SportSpacesRepository sportSpacesRepository, UserRepository userRepository
    , SportRepository sportRepository) {
        this.sportSpacesRepository = sportSpacesRepository;
        this.userRepository = userRepository;
        this.sportRepository = sportRepository;
    }

    @Override
    public Optional<SportSpaces> handle(Long userId, CreateSportSpacesCommand command) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var sport = sportRepository.findById((command.sportId()));
        if(!sportRepository.existsById(command.sportId())){
            throw new InvalidRoleTypeException("Sport id: " + command.sportId() + " already exists");
        }
        var sportSpaces = new SportSpaces(command, user, sport.get());
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