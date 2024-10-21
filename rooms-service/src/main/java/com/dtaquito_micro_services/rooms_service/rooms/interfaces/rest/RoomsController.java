package com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest;

import brave.Response;
import com.dtaquito_micro_services.rooms_service.chat.application.internal.commandservices.ChatRoomCommandServiceImpl;
import com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_micro_services.rooms_service.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_micro_services.rooms_service.rooms.client.SportSpaceClient;
import com.dtaquito_micro_services.rooms_service.rooms.client.UserClient;
import com.dtaquito_micro_services.rooms_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.SportSpaces;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.User;
import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_micro_services.rooms_service.rooms.application.internal.commandservices.RoomsCommandServiceImpl;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.Views;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_micro_services.rooms_service.rooms.domain.services.RoomsQueryService;
import com.dtaquito_micro_services.rooms_service.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.resources.RoomsResource;
import com.dtaquito_micro_services.rooms_service.rooms.interfaces.rest.transform.RoomsResourceFromEntityAssembler;

import com.fasterxml.jackson.annotation.JsonView;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.*;

@RestController
@Slf4j
@RequestMapping(value="/api/v1/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomsController {

    private final RoomsRepository roomsRepository;
    private final UserClient userClient;
    private final SportSpaceClient sportSpaceClient;
    private final RoomsQueryService roomsQueryService;
    private final PlayerListRepository playerListRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomCommandServiceImpl chatRoomCommandServiceImpl;
    private final RoomsResourceFromEntityAssembler roomsResourceFromEntityAssembler;
    private final TokenCacheService tokenCacheService;

    public RoomsController(RoomsRepository roomsRepository, UserClient userClient, SportSpaceClient sportSpaceClient, PlayerListRepository playerListRepository, ChatRoomRepository chatRoomRepository, ChatRoomCommandServiceImpl chatRoomCommandServiceImpl, RoomsQueryService roomsQueryService,
                           RoomsResourceFromEntityAssembler roomsResourceFromEntityAssembler, TokenCacheService tokenCacheService) {
        this.roomsRepository = roomsRepository;
        this.userClient = userClient;
        this.sportSpaceClient = sportSpaceClient;

        this.playerListRepository = playerListRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomCommandServiceImpl = chatRoomCommandServiceImpl;
        this.roomsQueryService = roomsQueryService;
        this.roomsResourceFromEntityAssembler = roomsResourceFromEntityAssembler;
        this.tokenCacheService = tokenCacheService;
    }

    @PostMapping("/create")
    @CircuitBreaker(name = "default", fallbackMethod = "createRoomFallback")
    public ResponseEntity<Map<String, String>> createRoom(@RequestParam Long creatorId, @RequestParam Long sportSpaceId, @RequestParam String day, @RequestParam String openingDate, @RequestParam String roomName) {
        try {
            // Validate input parameters
            if (creatorId == null || sportSpaceId == null || day == null || openingDate == null || roomName == null) {
                return ResponseEntity.status(400).body(Map.of("error", "All parameters are required"));
            }

            String token = tokenCacheService.getToken("default");
            User user = userClient.getUserById(creatorId, token).getBody();
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            SportSpaces sportSpace = sportSpaceClient.getSportSpaceById(sportSpaceId).getBody();
            if (sportSpace == null) {
                throw new IllegalArgumentException("Sport space not found");
            }

            BigDecimal amount = BigDecimal.valueOf(sportSpace.getAmount());
            BigDecimal userCredits = user.getCredits();

            log.info("User credits: {}", userCredits);
            log.info("Sport space amount: {}", amount);

            if (userCredits.compareTo(amount) < 0) {
                return ResponseEntity.status(400).body(Map.of("error", "Insufficient credits"));
            }

            // Parse openingDate to java.sql.Timestamp
            java.sql.Timestamp sqlOpeningDate;
            try {
                sqlOpeningDate = java.sql.Timestamp.valueOf(openingDate); // Parse the full date and time
            } catch (Exception e) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid opening date format"));
            }

            List<Rooms> existingRooms = roomsRepository.findBySportSpaceIdAndOpeningDate(sportSpaceId, sqlOpeningDate);
            for (Rooms room : existingRooms) {
                if (room.getDay().equals(day)) {
                    return ResponseEntity.status(400).body(Map.of("error", "Room already exists at the same time in this sport space"));
                }
            }

            // Deduct the advance payment from the user's credits
            userClient.deductCredits(user.getId(), amount.intValue());

            Rooms room = new Rooms();
            room.setCreatorId(user.getId());
            room.setSportSpaceId(sportSpaceId);
            room.setDay(day);
            room.setOpeningDate(sqlOpeningDate);
            room.setRoomName(roomName);
            room.setAccumulatedAmount(amount);
            roomsRepository.save(room);

            // Create and save ChatRoom
            ChatRoom chatRoom = chatRoomCommandServiceImpl.createChatRoom(room);

            // Check if the user is already in the player list
            if (!playerListRepository.existsByRoomAndUserId(room, user.getId())) {
                // Add the creator to the player list
                PlayerList playerList = new PlayerList();
                playerList.setRoom(room);
                playerList.setUserId(user.getId());
                playerList.setChatRoom(chatRoom);
                playerListRepository.save(playerList);
            }

            return ResponseEntity.ok(Map.of("message", "Room and ChatRoom created successfully"));
        } catch (ResourceAccessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating room", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    public ResponseEntity<Map<String, String>> createRoomFallback(Long creatorId, Long sportSpaceId, String day, String openingDate, String roomName, Throwable t) {
        log.error("Fallback triggered for createRoom: ", t);
        return ResponseEntity.status(503).body(Map.of("error", "Service unavailable"));
    }

    @GetMapping("{id}")
    @JsonView(Views.Public.class)
    @CircuitBreaker(name = "default", fallbackMethod = "getRoomByIdFallback")
    public ResponseEntity<RoomsResource> getRoomById(@PathVariable Long id) {
        try {
            Rooms room = roomsRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));
            return ResponseEntity.ok(roomsResourceFromEntityAssembler.toResourceFromEntity(room));
        } catch (ResourceAccessException e) {
            return getRoomByIdFallback(id, e);
        }

    }

    public ResponseEntity<RoomsResource> getRoomByIdFallback(Long id, Throwable t) {
        log.error("Fallback triggered for getRoomById: ", t);
        return ResponseEntity.status(503).body(new RoomsResource(id, null, null, null, null, null, null, null));
    }

    @GetMapping("/all")
    @JsonView(Views.Public.class)
    public ResponseEntity<List<RoomsResource>> getAllRooms() {
        var rooms = roomsQueryService.getAllRooms();
        var resource = rooms.stream()
                .map(roomsResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok().body(resource);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        Rooms room = roomsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        String token = tokenCacheService.getToken("default");

        //roomsCommandServiceImpl.refundToUsers(room.getId());

        User creator = userClient.getUserById(room.getCreatorId(), token).getBody();
        if (creator == null) {
            throw new IllegalArgumentException("Creator not found");
        }

        BigDecimal accumulatedAmount = room.getAccumulatedAmount();
        creator.setCredits(creator.getCredits().add(accumulatedAmount));
        userClient.updateCredits(creator.getId(), creator.getCredits().intValue());

        chatRoomRepository.deleteByRoomId(room.getId());

        room.getPlayerLists().clear();
        roomsRepository.delete(room);

        return ResponseEntity.ok("Room deleted and payments refunded");
    }
}