package com.dtaquito_micro_services.rooms_service.chat.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_micro_services.rooms_service.chat.domain.model.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatRoom(ChatRoom chatRoom);
}