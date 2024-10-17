package com.dtaquito_micro_services.rooms_service.chat.application.internal.commandservices;

import com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_micro_services.rooms_service.chat.domain.model.entities.Message;
import com.dtaquito_micro_services.rooms_service.chat.domain.services.ChatRoomCommandService;
import com.dtaquito_micro_services.rooms_service.chat.infrastructure.persistance.jpa.ChatRoomRepository;
import com.dtaquito_micro_services.rooms_service.chat.infrastructure.persistance.jpa.MessageRepository;
import com.dtaquito_micro_services.rooms_service.rooms.client.UserClient;
import com.dtaquito_micro_services.rooms_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.entities.User;
import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomCommandServiceImpl implements ChatRoomCommandService {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomCommandServiceImpl.class);

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserClient userClient;
    private final TokenCacheService tokenCacheService;

    @Autowired
    public ChatRoomCommandServiceImpl(ChatRoomRepository chatRoomRepository, MessageRepository messageRepository, UserClient userClient, TokenCacheService tokenCacheService) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.userClient = userClient;
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public ChatRoom createChatRoom(Rooms room) {
        ChatRoom chatRoom = new ChatRoom(room);
        chatRoom.setName(room.getRoomName());

        Long creator = room.getCreatorId();
        chatRoom.addPlayer(creator);

        for (PlayerList playerList : room.getPlayerLists()) {
            chatRoom.addPlayer(playerList.getUserId());
        }

        return chatRoomRepository.save(chatRoom);
    }

    public boolean isUserInRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
        if (chatRoom == null) {
            return false;
        }
        return chatRoom.getPlayerLists().stream()
                .anyMatch(playerList -> playerList.getUserId().equals(userId));
    }

    @Override
    public Message sendMessage(Long chatRoomId, String content, Long userId) {
        logger.debug("Attempting to send message to ChatRoom with id: {}", chatRoomId);

        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatRoomId);
        if (!chatRoomOptional.isPresent()) {
            logger.error("ChatRoom with id {} not found", chatRoomId);
            throw new IllegalArgumentException("ChatRoom not found");
        }

        ChatRoom chatRoom = chatRoomOptional.get();
        Message message = new Message();
        message.setContent(content);

        logger.debug("Attempting to find User with id: {}", userId);
        String token = tokenCacheService.getToken("default");
        ResponseEntity<User> userResponse = userClient.getUserById(userId, token);
        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            logger.error("User with id {} not found", userId);
            throw new IllegalArgumentException("User not found");
        }

        User user = userResponse.getBody();
        message.setUserId(user.getId());
        chatRoom.addMessage(message);
        chatRoomRepository.save(chatRoom);

        logger.debug("Message sent successfully to ChatRoom with id: {}", chatRoomId);
        return message;
    }

    @Override
    public List<Message> getMessages(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));
        return messageRepository.findByChatRoom(chatRoom);
    }
}