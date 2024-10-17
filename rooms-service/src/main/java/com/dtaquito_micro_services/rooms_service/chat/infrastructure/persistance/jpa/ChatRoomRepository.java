package com.dtaquito_micro_services.rooms_service.chat.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findById(Long id);
    Optional<ChatRoom> findByRoomId(Long roomId);
    void deleteByRoomId(Long roomId);

    @Query("SELECT c FROM ChatRoom c WHERE c.id = :roomId")
    Optional<ChatRoom> findChatRoomById(@Param("roomId") Long roomId);
}