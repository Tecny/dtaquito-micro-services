package com.dtaquito_micro_services.rooms_service.chat.domain.model.entities;

import com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates.ChatRoom;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    @JsonBackReference
    private ChatRoom chatRoom;

    private Long userId;

    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

}