package com.dtaquito_micro_services.deposit_service.deposit.domain.model.commands;

import java.math.BigDecimal;

public record CreateDepositCommand(Long userId, Long amount) {
}