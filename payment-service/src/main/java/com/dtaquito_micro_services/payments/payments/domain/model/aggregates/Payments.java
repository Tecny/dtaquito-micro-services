package com.dtaquito_micro_services.payments.payments.domain.model.aggregates;

import com.dtaquito_micro_services.payments.payments.domain.model.commands.CreatePaymentsCommand;
import com.dtaquito_micro_services.payments.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Payments extends AuditableAbstractAggregateRoot<Payments> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String userId;

    @Column
    private String transactionId;

    @Column
    private BigDecimal amount;

    @Column
    private String currency;

    @Column
    private String paymentStatus;

    public Payments() {}

    public Payments(CreatePaymentsCommand command, String userId, String transactionId, BigDecimal amount, String currency, String paymentStatus) {
        this.userId = userId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.paymentStatus = paymentStatus;
    }
}