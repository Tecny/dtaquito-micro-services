package com.dtaquito_backend.dtaquito_backend.player_list.domain.services;


import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.commands.CreatePlayerListCommand;

public interface PlayerListCommandService {
    PlayerList createPlayerList(CreatePlayerListCommand command);
}