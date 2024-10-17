package com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates;

import com.dtaquito_micro_services.rooms_service.chat.domain.model.aggregates.ChatRoom;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates.Rooms;
import com.dtaquito_micro_services.rooms_service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// PlayerList.java
@Entity
@Getter
@Setter
public class PlayerList extends AuditableAbstractAggregateRoot<PlayerList> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Rooms room;

    @Getter
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    public PlayerList() {}

    public PlayerList(Rooms room, Long userId) {
        this.room = room;
        this.userId = userId;
    }

    // Add a getter for roomId
    public Long getRoomId() {
        return room != null ? room.getId() : null;
    }

    // Add a getter for chatRoomId
    public Long getChatRoomId() {
        return chatRoom != null ? chatRoom.getId() : null;
    }

}