// Rooms.java
package com.dtaquito_backend.dtaquito_backend.rooms.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.player_list.domain.model.aggregates.PlayerList;
import com.dtaquito_backend.dtaquito_backend.rooms.domain.model.commands.CreateRoomCommand;
import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates.SportSpaces;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
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

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private String day;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date openingDate;

    @Column(nullable = false)
    private String roomName;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name="sport_space_id", nullable = false, foreignKey = @ForeignKey(name = "FK_sport_space_Id"))
    private SportSpaces sportSpace;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<PlayerList> playerLists = new ArrayList<>();


    @Column(name = "accumulated_amount", nullable = false)
    private BigDecimal accumulatedAmount = BigDecimal.ZERO;

    public Rooms() {}

    public Rooms(CreateRoomCommand command, User creator, SportSpaces sportSpace) {
        this.creator = creator;
        this.day = command.day();
        this.openingDate = command.openingDate();
        this.roomName = command.roomName();
        this.sportSpace = sportSpace;
        this.playerLists = command.playerLists();
    }
}