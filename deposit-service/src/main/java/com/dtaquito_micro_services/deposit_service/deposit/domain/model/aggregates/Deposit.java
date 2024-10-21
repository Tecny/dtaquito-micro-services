// Deposit.java
package com.dtaquito_micro_services.deposit_service.deposit.domain.model.aggregates;

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

    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;


    public Deposit() {
    }

    public Deposit(Long userId, Long amount) {
        this.userId = userId;
        this.amount = BigDecimal.valueOf(amount);}
}