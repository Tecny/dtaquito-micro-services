package com.dtaquito_micro_services.rooms_service.player_list.domain.services;


import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.player_list.domain.model.commands.CreatePlayerListCommand;

public interface PlayerListCommandService {
    PlayerList createPlayerList(CreatePlayerListCommand command);
}