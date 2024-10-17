package com.dtaquito_micro_services.deposit_service.deposit.application.internal.queryservices;

import com.dtaquito_micro_services.deposit_service.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_micro_services.deposit_service.deposit.domain.model.queries.GetDepositByIdQuery;
import com.dtaquito_micro_services.deposit_service.deposit.domain.services.DepositQueryService;
import com.dtaquito_micro_services.deposit_service.deposit.infrastructure.persistance.jpa.DepositRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepositQueryServiceImpl implements DepositQueryService {

    private final DepositRepository depositRepository;

    public DepositQueryServiceImpl(DepositRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    @Override
    public Optional<Deposit> handle(GetDepositByIdQuery query) {
        return depositRepository.findById(query.getDepositId());
    }
}