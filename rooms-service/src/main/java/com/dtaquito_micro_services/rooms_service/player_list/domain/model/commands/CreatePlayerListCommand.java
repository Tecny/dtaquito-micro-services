package com.dtaquito_micro_services.rooms_service.player_list.domain.model.commands;

import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.User;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;

public record CreatePlayerListCommand(Rooms room, User user) {}
