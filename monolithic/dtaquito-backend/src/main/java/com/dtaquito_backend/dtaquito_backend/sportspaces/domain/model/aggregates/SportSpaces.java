package com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.entities.Sport;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.valueObjects.GameMode;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.commands.CreateSportSpacesCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class SportSpaces extends AuditableAbstractAggregateRoot<SportSpaces> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_Id"))
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameMode gamemode;

    @Column(nullable = false)
    private Integer amount;

    public SportSpaces() {}

    public SportSpaces(CreateSportSpacesCommand command, User user, Sport sport) {
        this.name = command.name();
        this.sport = sport;
        this.imageUrl = command.imageUrl();
        this.price = command.price();
        this.district = command.district();
        this.description = command.description();
        this.user = user;
        this.startTime = command.startTime();
        this.endTime = command.endTime();
        this.gamemode = GameMode.valueOf(command.gamemode().toUpperCase());
        this.amount = command.amount();

        validateGameMode();
    }

    public void update(CreateSportSpacesCommand command, Sport sport) {
        this.name = command.name();
        this.sport = sport;
        this.imageUrl = command.imageUrl();
        this.price = command.price();
        this.district = command.district();
        this.description = command.description();
        this.startTime = command.startTime();
        this.endTime = command.endTime();
        this.gamemode = GameMode.valueOf(command.gamemode().toUpperCase());
        this.amount = command.amount();

        validateGameMode();
    }

    private void validateGameMode() {
        int maxPlayers = this.gamemode.getMaxPlayers();
        int calculatedAmount = (int) Math.floorDiv(this.price / 2, maxPlayers);

        if (this.amount > calculatedAmount) {
            throw new IllegalArgumentException("The advance amount exceeds the calculated amount based on the original price and game mode.");
        }
        if ((this.gamemode == GameMode.FUTBOL_11 || this.gamemode == GameMode.FUTBOL_7 || this.gamemode == GameMode.FUTBOL_8 || this.gamemode == GameMode.FUTBOL_5) && this.sport.getId() != 1) {
            throw new IllegalArgumentException("The sport ID must be 1 for the game modes FUTBOL_11, FUTBOL_7, FUTBOL_8, and FUTBOL_5");
        }
        if (this.gamemode == GameMode.BILLAR_3 && this.sport.getId() != 2) {
            throw new IllegalArgumentException("The sport ID must be 2 for the game mode BILLAR_3");
        }
    }
}