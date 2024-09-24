package com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.iam.domain.model.commands.SignUpCommand;
import com.dtaquito_backend.dtaquito_backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.commands.CreateUserCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Getter
@Setter
@Builder
public class User extends AuditableAbstractAggregateRoot<User> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private int allowedSportspaces = 0;

    @Column(nullable = false)
    private String plan;

    @Column(nullable = false)
    private String bankAccount;

    @Column(nullable = false)
    private BigDecimal credits = BigDecimal.ZERO;

    public User() {
    }

    public User(CreateUserCommand command, Role role) {
        this.name = command.name();
        this.email = command.email();
        this.password = command.password();
        this.role = role;
        this.plan = "free";
        this.bankAccount = command.bankAccount();
        setAllowedSportspacesBasedOnPlan();
    }

    public User(String name, String email, String password, Role role, String plan, String bankAccount) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.plan = plan;
        this.bankAccount = bankAccount;
        setAllowedSportspacesBasedOnPlan();
    }

    public User(Long id, String name, String email, String password, Role role, int allowedSportspaces, String plan, String bankAccount, BigDecimal credits) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.allowedSportspaces = allowedSportspaces;
        this.plan = plan;
        this.bankAccount = bankAccount;
        this.credits = credits;
    }

    public void setAllowedSportspacesBasedOnPlan() {
        switch (this.plan) {
            case "free":
                this.allowedSportspaces = 0;
                break;
            case "bronce":
                this.allowedSportspaces = 1;
                break;
            case "plata":
                this.allowedSportspaces = 2;
                break;
            case "oro":
                this.allowedSportspaces = 3;
                break;
            default:
                throw new IllegalArgumentException("Unknown plan type: " + this.plan);
        }
    }

    public void update(SignUpCommand command) {
        this.name = command.name();
        this.email = command.email();
        this.password = command.password();
    }

    public void updateAllowedSportspaces(int allowedSportspaces) {
        this.allowedSportspaces = allowedSportspaces;
    }

}