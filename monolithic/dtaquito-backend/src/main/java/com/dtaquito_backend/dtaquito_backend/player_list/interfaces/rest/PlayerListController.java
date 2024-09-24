package com.dtaquito_backend.dtaquito_backend.player_list.interfaces.rest;


import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_backend.dtaquito_backend.player_list.interfaces.rest.resources.PlayerListDTO;
import com.dtaquito_backend.dtaquito_backend.player_list.interfaces.rest.transform.PlayerListResourceFromEntityAssembler;
import com.dtaquito_backend.dtaquito_backend.rooms.application.internal.commandservices.RoomsCommandServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value="/api/v1/player-lists", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlayerListController {

    private final PlayerListRepository playerListRepository;
    private final RoomsCommandServiceImpl roomsCommandServiceImpl;

    public PlayerListController(PlayerListRepository playerListRepository, RoomsCommandServiceImpl roomsCommandServiceImpl) {
        this.playerListRepository = playerListRepository;
        this.roomsCommandServiceImpl = roomsCommandServiceImpl;
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinRoom(@RequestParam Long userId, @RequestParam Long roomId) {
        try {
            roomsCommandServiceImpl.addPlayerToRoomAndChat(roomId, userId);
            return ResponseEntity.ok(Map.of("message", "Player added to room and chat successfully"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            roomsCommandServiceImpl.refundToUsers(userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException e) {
            roomsCommandServiceImpl.refundToUsers(userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Query did not return a unique result"));
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<PlayerListDTO>> getPlayerListsByRoomId(@PathVariable Long roomId) {
        List<PlayerList> playerLists = playerListRepository.findByRoomId(roomId);
        List<PlayerListDTO> playerListDTOs = playerLists.stream()
                .map(PlayerListResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(playerListDTOs);
    }
}