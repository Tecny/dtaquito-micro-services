package com.dtaquito_backend.dtaquito_backend.player_list.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.commands.CreatePlayerListCommand;
import com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerListCommandServiceImpl implements com.dtaquito_backend.dtaquito_backend.player_list.domain.services.PlayerListCommandService {


    private final PlayerListRepository playerListRepository;

    private PlayerListCommandServiceImpl(PlayerListRepository playerListRepository) {
        this.playerListRepository = playerListRepository;
    }

    @Override
    public PlayerList createPlayerList(CreatePlayerListCommand command) {
        PlayerList playerList = new PlayerList(command.room(), command.user());
        return playerListRepository.save(playerList);
    }
}