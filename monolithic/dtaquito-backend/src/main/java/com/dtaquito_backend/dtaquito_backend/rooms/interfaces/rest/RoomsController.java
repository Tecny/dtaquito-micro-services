package com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.chat.application.internal.commandservices.ChatRoomCommandServiceImpl;
import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_backend.dtaquito_backend.rooms.application.internal.commandservices.RoomsCommandServiceImpl;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.Views;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.services.RoomsQueryService;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.resources.RoomsResource;
import com.dtaquito_backend.dtaquito_backend.rooms.interfaces.rest.transform.RoomsResourceFromEntityAssembler;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.sportspaces.infrastructure.persistance.jpa.SportSpacesRepository;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;


@RestController
@Slf4j
@RequestMapping(value="/api/v1/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomsController {

    private final RoomsRepository roomsRepository;
    private final UserRepository userRepository;
    private final SportSpacesRepository sportSpacesRepository;
    private final RoomsQueryService roomsQueryService;
    private final RoomsCommandServiceImpl roomsCommandServiceImpl;
    private final PlayerListRepository playerListRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomCommandServiceImpl chatRoomCommandServiceImpl;

    public RoomsController(RoomsRepository roomsRepository, UserRepository userRepository, SportSpacesRepository sportSpacesRepository, RoomsCommandServiceImpl roomsCommandServiceImpl, PlayerListRepository playerListRepository, ChatRoomRepository chatRoomRepository, ChatRoomCommandServiceImpl chatRoomCommandServiceImpl, RoomsQueryService roomsQueryService) {
        this.roomsRepository = roomsRepository;
        this.userRepository = userRepository;
        this.sportSpacesRepository = sportSpacesRepository;
        this.roomsCommandServiceImpl = roomsCommandServiceImpl;
        this.playerListRepository = playerListRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomCommandServiceImpl = chatRoomCommandServiceImpl;
        this.roomsQueryService = roomsQueryService;

    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createRoom(@RequestParam Long creatorId, @RequestParam Long sportSpaceId, @RequestParam String day, @RequestParam String openingDate, @RequestParam String roomName) {
        SportSpaces sportSpace = sportSpacesRepository.findById(sportSpaceId)
                .orElseThrow(() -> new IllegalArgumentException("Sport space not found"));

        try {
            User user = userRepository.findById(creatorId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            BigDecimal amount = BigDecimal.valueOf(sportSpace.getAmount());
            if (user.getCredits().compareTo(amount) < 0) {
                return ResponseEntity.status(400).body(Map.of("error", "Insufficient credits"));
            }

            if (day == null || openingDate == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Day and opening date cannot be null"));
            }

            // Parse openingDate to java.sql.Timestamp
            java.sql.Timestamp sqlOpeningDate;
            try {
                sqlOpeningDate = java.sql.Timestamp.valueOf(openingDate); // Parse the full date and time
            } catch (Exception e) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid opening date format"));
            }

            List<Rooms> existingRooms = roomsRepository.findBySportSpaceAndOpeningDate(sportSpace, sqlOpeningDate);
            for (Rooms room : existingRooms) {
                if (room.getDay().equals(day)) {
                    return ResponseEntity.status(400).body(Map.of("error", "Room already exists at the same time in this sport space"));
                }
            }

            user.setCredits(user.getCredits().subtract(amount));
            userRepository.save(user);

            Rooms room = new Rooms();
            room.setCreator(user);
            room.setSportSpace(sportSpace);
            room.setDay(day);
            room.setOpeningDate(sqlOpeningDate);
            room.setRoomName(roomName);
            room.setAccumulatedAmount(amount);
            roomsRepository.save(room);

            // Create and save ChatRoom
            ChatRoom chatRoom = chatRoomCommandServiceImpl.createChatRoom(room);

            // Check if the user is already in the player list
            if (!playerListRepository.existsByRoomAndUser(room, user)) {
                // Add the creator to the player list
                PlayerList playerList = new PlayerList();
                playerList.setRoom(room);
                playerList.setUser(user);
                playerList.setChatRoom(chatRoom);
                playerListRepository.save(playerList);
            }

            return ResponseEntity.ok(Map.of("message", "Room and ChatRoom created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    @JsonView(Views.Public.class)
    public ResponseEntity<List<RoomsResource>> getAllRooms() {
        var rooms = roomsQueryService.getAllRooms();
        var resource = rooms.stream().map(RoomsResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok().body(resource);
    }

    @GetMapping("{id}")
    @JsonView(Views.Public.class)
    public ResponseEntity<RoomsResource> getRoomById(@PathVariable Long id) {
        Rooms room = roomsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        return ResponseEntity.ok(RoomsResourceFromEntityAssembler.toResourceFromEntity(room));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        Rooms room = roomsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        roomsCommandServiceImpl.refundToUsers(room.getId());

        User creator = userRepository.findById(room.getCreator().getId())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        BigDecimal accumulatedAmount = room.getAccumulatedAmount();
        creator.setCredits(creator.getCredits().add(accumulatedAmount));
        userRepository.save(creator);

        chatRoomRepository.deleteByRoomId(room.getId());

        room.getPlayerLists().clear();
        roomsRepository.delete(room);

        return ResponseEntity.ok("Room deleted and payments refunded");
    }
}