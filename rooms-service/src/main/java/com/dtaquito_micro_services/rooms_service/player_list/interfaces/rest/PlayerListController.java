package com.dtaquito_micro_services.rooms_service.player_list.interfaces.rest;


import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_micro_services.rooms_service.player_list.interfaces.rest.resources.PlayerListDTO;
import com.dtaquito_micro_services.rooms_service.player_list.interfaces.rest.transform.PlayerListResourceFromEntityAssembler;
import com.dtaquito_micro_services.rooms_service.rooms.application.internal.commandservices.RoomsCommandServiceImpl;
import com.dtaquito_micro_services.rooms_service.rooms.domain.services.RoomsCommandService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value="/api/v1/player-lists", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlayerListController {

    private final PlayerListRepository playerListRepository;
    private final RoomsCommandService roomsCommandServiceImpl;

    public PlayerListController(PlayerListRepository playerListRepository, RoomsCommandServiceImpl roomsCommandServiceImpl) {
        this.playerListRepository = playerListRepository;
        this.roomsCommandServiceImpl = roomsCommandServiceImpl;
    }

    @PostMapping("/join")
    @CircuitBreaker(name = "default", fallbackMethod = "joinRoomFallback")
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
        } catch (ResourceAccessException e) {
            throw e;
        }
    }

    public ResponseEntity<Map<String, String>> joinRoomFallback(Long userId, Long roomId, Throwable e) {
        log.error("Error joining room: " + roomId + " with user: " + userId);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Service unavailable"));
    }

    @GetMapping("/room/{roomId}")
    @CircuitBreaker(name = "default", fallbackMethod = "getPlayerListsByRoomIdFallback")
    public ResponseEntity<List<PlayerListDTO>> getPlayerListsByRoomId(@PathVariable Long roomId) {
        try {
            List<PlayerList> playerLists = playerListRepository.findByRoomId(roomId);
            List<PlayerListDTO> playerListDTOs = playerLists.stream()
                    .map(PlayerListResourceFromEntityAssembler::toResourceFromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(playerListDTOs);
        } catch (ResourceAccessException e) {
            return getPlayerListsByRoomIdFallback(roomId, e);
        }
    }

    public ResponseEntity<List<PlayerListDTO>> getPlayerListsByRoomIdFallback(Long roomId, Throwable e) {
        log.error("Error getting player lists by room id: " + roomId);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Collections.emptyList());
    }
}