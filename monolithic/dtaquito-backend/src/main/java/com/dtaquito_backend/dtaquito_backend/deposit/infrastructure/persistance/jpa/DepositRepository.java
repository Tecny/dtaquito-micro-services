package com.dtaquito_backend.dtaquito_backend.deposit.infrastructure.persistance.jpa;

import com.dtaquito_backend.dtaquito_backend.deposit.domain.model.aggregates.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
    void deleteByUserId(Long userId);
}