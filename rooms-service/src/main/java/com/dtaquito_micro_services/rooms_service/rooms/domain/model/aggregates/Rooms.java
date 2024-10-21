// Rooms.java
package com.dtaquito_micro_services.rooms_service.rooms.domain.model.aggregates;

import com.dtaquito_micro_services.rooms_service.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_micro_services.rooms_service.rooms.domain.model.commands.CreateRoomCommand;
import com.dtaquito_micro_services.rooms_service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
public class Rooms extends AuditableAbstractAggregateRoot<Rooms> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long creatorId;

    @Column(nullable = false)
    private String day;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date openingDate;

    @Column(nullable = false)
    private String roomName;

    private Long sportSpaceId;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<PlayerList> playerLists = new ArrayList<>();


    @Column(name = "accumulated_amount", nullable = false)
    private BigDecimal accumulatedAmount = BigDecimal.ZERO;

    public Rooms() {}

    public Rooms(CreateRoomCommand command, Long creatorId, Long sportSpaceId) {

        this.creatorId = creatorId;
        this.day = command.day();
        this.openingDate = command.openingDate();
        this.roomName = command.roomName();
        this.sportSpaceId = sportSpaceId;
        this.playerLists = command.playerLists();
    }
}