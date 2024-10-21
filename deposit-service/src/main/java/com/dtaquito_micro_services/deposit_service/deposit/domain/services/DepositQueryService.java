package com.dtaquito_micro_services.deposit_service.deposit.domain.services;

import com.dtaquito_micro_services.deposit_service.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_micro_services.deposit_service.deposit.domain.model.queries.GetDepositByIdQuery;

import java.util.Optional;

public interface DepositQueryService {

    Optional<Deposit> handle(GetDepositByIdQuery query);
}
