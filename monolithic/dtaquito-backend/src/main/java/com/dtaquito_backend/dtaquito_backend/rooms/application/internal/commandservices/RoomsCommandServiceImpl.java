package com.dtaquito_backend.dtaquito_backend.rooms.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_backend.dtaquito_backend.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_backend.dtaquito_backend.deposit.infrastructure.persistance.jpa.DepositRepository;
import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.player_list.infrastructure.persistance.jpa.PlayerListRepository;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.services.RoomsCommandService;
import com.dtaquito_backend.dtaquito_backend.rooms.infrastructure.persistance.jpa.RoomsRepository;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.transaction.Transactional;import org.springframework.scheduling.annotation.Scheduled;
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
    private final UserRepository userRepository;
    private final PlayerListRepository playerListRepository;
    private final DepositRepository depositRepository;
    private final ChatRoomRepository chatRoomRepository;

    public RoomsCommandServiceImpl(RoomsRepository roomsRepository,
                                   APIContext apiContext, UserRepository userRepository, PlayerListRepository playerListRepository, DepositRepository depositRepository, ChatRoomRepository chatRoomRepository) {
        this.roomsRepository = roomsRepository;
        this.apiContext = apiContext;
        this.userRepository = userRepository;
        this.playerListRepository = playerListRepository;
        this.depositRepository = depositRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
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
                if (room.getCreator().getId() == null) {
                    logger.warn("Room with ID {} has a null creator_id and cannot be deleted.", room.getId());
                    continue;
                }

                try {
                    if (room.getPlayerLists().size() >= room.getSportSpace().getGamemode().getMaxPlayers()) {
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

                    // Delete deposits associated with the users in the room
                    for (PlayerList playerList : room.getPlayerLists()) {
                        User user = playerList.getUser();
                        depositRepository.deleteByUserId(user.getId());
                    }

                    // Delete the room
                    roomsRepository.delete(room);

                    logger.info("Room with ID {} deleted successfully.", room.getId());
                } catch (Exception e) {
                    logger.error("Error handling room with ID {}: {}", room.getId(), e.getMessage(), e);
                }
            }
        }
    }

    public void transferToCreator(Rooms room) {
        User sportSpaceOwner = room.getSportSpace().getUser();
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

                // Restablecer el monto acumulado a 0
                room.setAccumulatedAmount(BigDecimal.ZERO);
                roomsRepository.save(room);
            } catch (PayPalRESTException e) {
                logger.error("Error creating payout: {}", e.getMessage(), e);
            }
        }
    }

    public void refundToUsers(Long playerListsId) {
        Optional<PlayerList> optionalPlayerList = playerListRepository.findById(playerListsId);
        if (optionalPlayerList.isEmpty()) {
            logger.warn("PlayerList with ID {} not found", playerListsId);
            return;
        }

        PlayerList playerList = optionalPlayerList.get();
        Rooms room = playerList.getRoom();
        BigDecimal accumulatedAmount = room.getAccumulatedAmount();
        List<User> users = room.getPlayerLists().stream().map(PlayerList::getUser).toList();

        if (users.isEmpty()) {
            logger.warn("No users to refund in room: {}", room.getId());
            return;
        }

        BigDecimal amountPerUser = accumulatedAmount.divide(BigDecimal.valueOf(users.size()), RoundingMode.HALF_UP);

        for (User user : users) {
            user.setCredits(user.getCredits().add(amountPerUser));
            userRepository.save(user);
        }

        playerListRepository.deleteByRoomId(room.getId());
    }

    public void addPlayerToRoomAndChat(Long roomId, Long userId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int maxPlayers = room.getSportSpace().getGamemode().getMaxPlayers();
        if (room.getPlayerLists().size() >= maxPlayers) {
            throw new IllegalStateException("Room is already full");
        }

        if (playerListRepository.existsByRoomAndUser(room, user)) {
            throw new IllegalStateException("User already in the room");
        }

        BigDecimal amount = BigDecimal.valueOf(room.getSportSpace().getAmount());
        if (user.getCredits().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient credits. Please recharge.");
        }

        user.setCredits(user.getCredits().subtract(amount));
        userRepository.save(user);

        room.setAccumulatedAmount(room.getAccumulatedAmount().add(amount));
        roomsRepository.save(room);

        PlayerList playerList = new PlayerList();
        playerList.setRoom(room);
        playerList.setUser(user);

        List<PlayerList> playerLists = playerListRepository.findByRoomId(roomId);
        if (playerLists.isEmpty()) {
            throw new IllegalStateException("No chat rooms associated with roomId: " + roomId);
        }
        ChatRoom chatRoom = playerLists.get(0).getChatRoom();
        playerList.setChatRoom(chatRoom);

        playerListRepository.save(playerList);
    }
}