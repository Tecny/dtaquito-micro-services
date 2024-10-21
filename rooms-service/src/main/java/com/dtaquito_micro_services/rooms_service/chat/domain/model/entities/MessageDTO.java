package com.dtaquito_micro_services.rooms_service.chat.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class MessageDTO {
    private String content;
    private Long userId;
    private LocalDateTime createdAt;

    public MessageDTO(String content, Long userId, LocalDateTime createdAt) {
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
    }
}