// Deposit.java
package com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "deposits")
@Getter
@Setter
public class Deposit {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amount;


    public Deposit() {
    }

    public Deposit(User user, BigDecimal amount) {
        this.user = user;
        this.amount = amount;
    }
}