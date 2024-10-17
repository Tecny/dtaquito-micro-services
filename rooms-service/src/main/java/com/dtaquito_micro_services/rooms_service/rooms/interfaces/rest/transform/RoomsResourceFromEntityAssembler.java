package com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.transform;

import com.dtaquito_micro_services.rooms_service.rooms.client.SportSpaceClient;
import com.dtaquito_micro_services.rooms_service.rooms.client.UserClient;
import com.dtaquito_micro_services.rooms_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.SportSpaces;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.User;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources.RoomsResource;
import com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources.SportSpaceDTO;
import com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources.UserDTO;
import com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources.PlayerListUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoomsResourceFromEntityAssembler {

    private final UserClient userClient;
    private final SportSpaceClient sportSpaceClient;
    private final TokenCacheService tokenCacheService;

    @Autowired
    public RoomsResourceFromEntityAssembler(UserClient userClient, SportSpaceClient sportSpaceClient, TokenCacheService tokenCacheService) {
        this.userClient = userClient;
        this.sportSpaceClient = sportSpaceClient;
        this.tokenCacheService = tokenCacheService;
    }

    public RoomsResource toResourceFromEntity(Rooms room) {
        String token = tokenCacheService.getToken("default");
        User creator = userClient.getUserById(room.getCreatorId(), token).getBody();
        SportSpaces sportSpace = sportSpaceClient.getSportSpaceById(room.getSportSpaceId()).getBody();

        UserDTO creatorDTO = new UserDTO(
                creator.getId(),
                creator.getName(),
                creator.getEmail()
        );

        SportSpaceDTO sportSpaceDTO = new SportSpaceDTO(
                sportSpace.getId(),
                sportSpace.getName(),
                sportSpace.getSportId(),
                sportSpace.getImageUrl(),
                new BigDecimal(sportSpace.getPrice()), // Convert Long to BigDecimal
                sportSpace.getDistrict(),
                sportSpace.getDescription(),
                sportSpace.getStartTime(),
                sportSpace.getEndTime(),
                sportSpace.getGamemode().toString(),
                sportSpace.getAmount()
        );

        List<PlayerListUserDTO> playerListUserDTOs = room.getPlayerLists().stream()
                .map(playerList -> new PlayerListUserDTO(playerList.getUserId()))
                .collect(Collectors.toList());

        return new RoomsResource(
                room.getId(),
                creatorDTO,
                room.getDay(),
                room.getOpeningDate(),
                room.getRoomName(),
                sportSpaceDTO,
                playerListUserDTOs,
                room.getAccumulatedAmount()
        );
    }
}