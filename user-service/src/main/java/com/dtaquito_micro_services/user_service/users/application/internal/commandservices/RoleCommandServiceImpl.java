package com.dtaquito_micro_services.user_service.users.application.internal.commandservices;


import com.dtaquito_micro_services.user_service.users.domain.model.commands.SeedRoleTypeCommand;
import com.dtaquito_micro_services.user_service.users.domain.model.entities.Role;
import com.dtaquito_micro_services.user_service.users.domain.model.valueObjects.RoleTypes;
import com.dtaquito_micro_services.user_service.users.domain.services.RoleCommandService;
import com.dtaquito_micro_services.user_service.users.infrastructure.persistance.jpa.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        handle(new SeedRoleTypeCommand());
    }

    @Override
    public void handle(SeedRoleTypeCommand command){
        Arrays.stream(RoleTypes.values()).forEach(roleType -> {
            if (!roleRepository.existsByRoleType(roleType)) {
                roleRepository.save(new Role(roleType));
            }
        });
    }
}
