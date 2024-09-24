package com.dtaquito_backend.dtaquito_backend.player_list.domain.model.commands;

import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;

public record CreatePlayerListCommand(Rooms room, User user) {}
