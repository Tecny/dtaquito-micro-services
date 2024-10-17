package com.dtaquito_micro_services.deposit_service.deposit.domain.services;

import com.dtaquito_micro_services.deposit_service.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_micro_services.deposit_service.deposit.domain.model.commands.CreateDepositCommand;

import java.util.Optional;

public interface DepositCommandService {

    Optional<Deposit> handle(CreateDepositCommand command);
    void save(Deposit deposit);
}
