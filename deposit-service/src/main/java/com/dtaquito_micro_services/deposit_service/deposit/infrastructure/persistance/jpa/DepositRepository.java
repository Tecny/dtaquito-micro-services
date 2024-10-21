package com.dtaquito_micro_services.deposit_service.deposit.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.deposit_service.deposit.domain.model.aggregates.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    void deleteByUserId(Long userId);
}