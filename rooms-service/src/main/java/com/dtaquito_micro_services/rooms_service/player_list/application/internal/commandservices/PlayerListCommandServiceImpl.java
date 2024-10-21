package com.dtaquito_micro_services.rooms_service.player_list.application.internal.commandservices;

import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.player_list.domain.model.commands.CreatePlayerListCommand;
import com.dtaquito_micro_services.rooms_service.player_list.domain.services.PlayerListCommandService;
import com.dtaquito_micro_services.rooms_service.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerListCommandServiceImpl implements PlayerListCommandService {


    private final PlayerListRepository playerListRepository;

    private PlayerListCommandServiceImpl(PlayerListRepository playerListRepository) {
        this.playerListRepository = playerListRepository;
    }

    @Override
    public PlayerList createPlayerList(CreatePlayerListCommand command) {
        PlayerList playerList = new PlayerList(command.room(), command.user().getId());
        return playerListRepository.save(playerList);
    }
}