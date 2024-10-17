package com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlayerListUserDTO {
    private Long userId;

    public PlayerListUserDTO(Long userId) {
        this.userId = userId;
    }
}