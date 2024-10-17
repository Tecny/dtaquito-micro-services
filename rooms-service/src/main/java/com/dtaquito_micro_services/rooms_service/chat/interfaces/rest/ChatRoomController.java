package com.dtaquito_micro_services.rooms_service.chat.interfaces.rest;

import com.dtaquito_micro_services.rooms_service.chat.application.internal.commandservices.ChatRoomCommandServiceImpl;
import com.dtaquito_micro_services.rooms_service.chat.domain.config.ChatWebSocketHandler;
import com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_micro_services.rooms_service.chat.domain.model.entities.Message;
import com.dtaquito_micro_services.rooms_service.chat.domain.model.entities.MessageDTO;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.socket.TextMessage;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatRoomController {

    private final ChatRoomCommandServiceImpl chatRoomService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatRoomController(ChatRoomCommandServiceImpl chatRoomService, ChatWebSocketHandler chatWebSocketHandler) {
        this.chatRoomService = chatRoomService;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody Rooms room) {
        try {
            ChatRoom chatRoom = chatRoomService.createChatRoom(room);
            return ResponseEntity.ok(chatRoom);
        }
        catch (ResourceAccessException e) {
            return createChatRoomFallback(room, e);
        }
    }

    public ResponseEntity<ChatRoom> createChatRoomFallback(Rooms room, Throwable e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
    }

    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(@PathVariable Long chatRoomId, @RequestBody String content, @RequestParam Long userId) {
        // Verificar si el usuario pertenece a la sala
        if (!chatRoomService.isUserInRoom(chatRoomId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Message message = chatRoomService.sendMessage(chatRoomId, content, userId);
        MessageDTO responseDTO = new MessageDTO(message.getContent(), message.getUserId(), message.getCreatedAt());

        try {
            // Log para verificar que el mensaje se está enviando
            System.out.println("Enviando mensaje a través de WebSocket: " + responseDTO.getContent());

            // Enviar mensaje a través de WebSocket
            chatWebSocketHandler.broadcastMessage(new TextMessage(responseDTO.getContent()));
        } catch (Exception e) {
            // Log de la excepción
            e.printStackTrace();

            // Manejar la excepción y devolver una respuesta de error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long chatRoomId) {
        List<Message> messages = chatRoomService.getMessages(chatRoomId);
        List<MessageDTO> responseDTOs = messages.stream()
                .map(message -> new MessageDTO(message.getContent(), message.getUserId(), message.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }
}