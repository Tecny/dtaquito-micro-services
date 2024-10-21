// ChatRoom.java
package com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates;

import com.dtaquito_micro_services.rooms_service.chat.domain.model.entities.Message;
import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_micro_services.rooms_service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom extends AuditableAbstractAggregateRoot<ChatRoom> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Rooms room;

    private String name;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerList> playerLists = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Message> messages = new ArrayList<>();

    public ChatRoom(Rooms room) {
        this.room = room;
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.setChatRoom(this);
    }

    public void addPlayer(Long userId) {
        PlayerList playerList = new PlayerList();
        playerList.setUserId(userId);
        playerList.setChatRoom(this);
        playerList.setRoom(this.room);
        playerLists.add(playerList);
    }
}