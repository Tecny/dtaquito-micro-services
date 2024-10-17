package com.dtaquito_micro_services.rooms_service.rooms.application.internal.commandservices;

import com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_micro_services.rooms_service.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_micro_services.rooms_service.rooms.client.SportSpaceClient;
import com.dtaquito_micro_services.rooms_service.rooms.client.UserClient;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.SportSpaces;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.User;
import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_micro_services.rooms_service.rooms.domain.services.RoomsCommandService;
import com.dtaquito_micro_services.rooms_service.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RoomsCommandServiceImpl implements RoomsCommandService {

    private final Logger logger = LoggerFactory.getLogger(RoomsCommandServiceImpl.class);
    private final RoomsRepository roomsRepository;
    private final APIContext apiContext;
    private final UserClient userClient;
    private final SportSpaceClient sportSpaceClient;
    private final PlayerListRepository playerListRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    public RoomsCommandServiceImpl(RoomsRepository roomsRepository,
                                   APIContext apiContext, UserClient userClient, SportSpaceClient sportSpaceClient, PlayerListRepository playerListRepository, ChatRoomRepository chatRoomRepository) {
        this.roomsRepository = roomsRepository;
        this.apiContext = apiContext;
        this.userClient = userClient;
        this.sportSpaceClient = sportSpaceClient;
        this.playerListRepository = playerListRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    @Override
    public void handleRooms() {
        List<Rooms> rooms = roomsRepository.findAll();
        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("America/Lima"));

        for (Rooms room : rooms) {
            LocalDate roomDate = room.getOpeningDate().toInstant()
                    .atZone(ZoneId.of("America/Lima"))
                    .toLocalDate();
            LocalTime roomTime = LocalTime.parse(room.getDay(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime roomDateTime = LocalDateTime.of(roomDate, roomTime);

            if (roomDateTime.isBefore(currentDateTime)) {
                if (room.getCreatorId() == null) {
                    logger.warn("Room with ID {} has a null creator_id and cannot be deleted.", room.getId());
                    continue;
                }

                try {
                    if (room.getPlayerLists().size() >= getSportSpace(room.getSportSpaceId()).getGamemode().getMaxPlayers()) {
                        transferToCreator(room);
                        // Skip deletion if the room is full
                        continue;
                    } else {
                        // Check if playerLists is not empty before refunding
                        if (!room.getPlayerLists().isEmpty()) {
                            refundToUsers(room.getPlayerLists().get(0).getId());
                        }
                    }

                    // Delete messages associated with the chat room
                    Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByRoomId(room.getId());
                    if (chatRoomOptional.isPresent()) {
                        ChatRoom chatRoom = chatRoomOptional.get();
                        chatRoom.getMessages().clear();
                        chatRoomRepository.save(chatRoom);
                    }

                    // Delete the associated ChatRoom
                    chatRoomRepository.deleteByRoomId(room.getId());

                    // Delete player lists associated with the room
                    playerListRepository.deleteByRoomId(room.getId());

                    // Delete the room
                    roomsRepository.delete(room);

                    logger.info("Room with ID {} deleted successfully.", room.getId());
                } catch (Exception e) {
                    logger.error("Error handling room with ID {}: {}", room.getId(), e.getMessage(), e);
                }
            }
        }
    }

    public SportSpaces getSportSpace(Long sportSpaceId) {
        ResponseEntity<SportSpaces> response = sportSpaceClient.getSportSpaceByIdWithPredefinedToken(sportSpaceId);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("SportSpace not found");
        }
        return response.getBody();
    }

    @Override
    public void transferToCreator(Rooms room) {
        User sportSpaceOwner = userClient.getUserByIdWithPredefinedToken(getSportSpace(room.getSportSpaceId()).getUserId()).getBody();
        BigDecimal accumulatedAmount = room.getAccumulatedAmount();

        if (sportSpaceOwner != null && sportSpaceOwner.getBankAccount() != null && !sportSpaceOwner.getBankAccount().isEmpty()) {
            try {
                Payout payout = new Payout();
                PayoutSenderBatchHeader senderBatchHeader = new PayoutSenderBatchHeader()
                        .setSenderBatchId("batch_" + System.currentTimeMillis())
                        .setEmailSubject("Payment received!");

                Currency currency = new Currency()
                        .setValue(accumulatedAmount.toString())
                        .setCurrency("USD");

                PayoutItem senderItem = new PayoutItem()
                        .setRecipientType("EMAIL")
                        .setReceiver(sportSpaceOwner.getBankAccount())
                        .setAmount(currency)
                        .setSenderItemId("item_" + System.currentTimeMillis())
                        .setNote("Payment for room");

                List<PayoutItem> items = new ArrayList<>();
                items.add(senderItem);

                payout.setSenderBatchHeader(senderBatchHeader)
                        .setItems(items);

                PayoutBatch batch = payout.create(apiContext, new HashMap<>());
                logger.info("Payout created with batch id: {}", batch.getBatchHeader().getPayoutBatchId());

                // Reset the accumulated amount to 0
                room.setAccumulatedAmount(BigDecimal.ZERO);
                roomsRepository.save(room);
            } catch (PayPalRESTException e) {
                logger.error("Error creating payout: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void refundToUsers(Long playerListsId) {

        Optional<PlayerList> optionalPlayerList = playerListRepository.findById(playerListsId);
        if (optionalPlayerList.isEmpty()) {
            logger.warn("PlayerList with ID {} not found", playerListsId);
            return;
        }

        PlayerList playerList = optionalPlayerList.get();
        Rooms room = playerList.getRoom();
        BigDecimal accumulatedAmount = room.getAccumulatedAmount();
        List<Long> userIds = room.getPlayerLists().stream().map(PlayerList::getUserId).toList();

        if (userIds.isEmpty()) {
            logger.warn("No users to refund in room: {}", room.getId());
            return;
        }

        BigDecimal amountPerUser = accumulatedAmount.divide(BigDecimal.valueOf(userIds.size()), RoundingMode.HALF_UP);

        for (Long userId : userIds) {
            ResponseEntity<User> userResponse = userClient.getUserByIdWithPredefinedToken(userId);
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                User user = userResponse.getBody();
                user.setCredits(user.getCredits().add(amountPerUser));
                // Assuming you have a method to update user credits via UserClient
                userClient.updateCredits(user.getId(), amountPerUser.intValue());
            }
        }

        playerListRepository.deleteByRoomId(room.getId());
    }

    @Override
    public void addPlayerToRoomAndChat(Long roomId, Long userId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        ResponseEntity<User> userResponse = userClient.getUserByIdWithPredefinedToken(userId);
        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userResponse.getBody();

        int maxPlayers = getSportSpace(room.getSportSpaceId()).getGamemode().getMaxPlayers();
        if (room.getPlayerLists().size() >= maxPlayers) {
            throw new IllegalStateException("Room is already full");
        }

        // Check if the user is already in the player list
        if (playerListRepository.existsByRoomAndUserId(room, userId)) {
            throw new IllegalStateException("User already in the room");
        }

        BigDecimal amount = BigDecimal.valueOf(getSportSpace(room.getSportSpaceId()).getAmount());
        if (user.getCredits().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient credits. Please recharge.");
        }

        // Deduct the advance payment from the user's credits
        userClient.deductCredits(user.getId(), amount.intValue());

        room.setAccumulatedAmount(room.getAccumulatedAmount().add(amount));
        roomsRepository.save(room);

        PlayerList playerList = new PlayerList();
        playerList.setRoom(room);
        playerList.setUserId(userId);

        // Assign the chatRoom from the first playerList found
        List<PlayerList> playerLists = playerListRepository.findByRoomId(roomId);
        if (playerLists.isEmpty()) {
            throw new IllegalStateException("No chat rooms associated with roomId: " + roomId);
        }
        ChatRoom chatRoom = playerLists.get(0).getChatRoom();
        playerList.setChatRoom(chatRoom);

        playerListRepository.save(playerList);
    }
}