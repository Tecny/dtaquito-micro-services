package com.dtaquito_backend.dtaquito_backend.chat.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MessageDTO {
    private String content;
    private Long userId;

    // Constructor
    public MessageDTO(String content, Long userId) {
        this.content = content;
        this.userId = userId;
    }
}