package com.dtaquito_micro_services.deposit_service.deposit.application.internal.commandservices;

import com.dtaquito_micro_services.deposit_service.config.impl.TokenCacheService;
import com.dtaquito_micro_services.deposit_service.deposit.client.UserClient;
import com.dtaquito_micro_services.deposit_service.deposit.domain.model.aggregates.Deposit;
import com.dtaquito_micro_services.deposit_service.deposit.domain.model.aggregates.User;
import com.dtaquito_micro_services.deposit_service.deposit.domain.model.commands.CreateDepositCommand;
import com.dtaquito_micro_services.deposit_service.deposit.domain.services.DepositCommandService;
import com.dtaquito_micro_services.deposit_service.deposit.infrastructure.persistance.jpa.DepositRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DepositCommandServiceImpl implements DepositCommandService {

    private final DepositRepository depositRepository;
    private final UserClient userClient;
    private final TokenCacheService tokenCacheService;

    public DepositCommandServiceImpl(DepositRepository depositRepository, UserClient userClient, TokenCacheService tokenCacheService) {
        this.depositRepository = depositRepository;
        this.userClient = userClient;
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public Optional<Deposit> handle(CreateDepositCommand command) {
        String token = tokenCacheService.getToken("default");
        ResponseEntity<User> response = userClient.getUserById(command.userId(), token);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("User not found");
        }
        var user = response.getBody();

        // Use the amount directly from the command
        Long amount = command.amount();

        // Create and save deposit
        var deposit = new Deposit(user.getId(), amount);
        var createdDeposit = depositRepository.save(deposit);

        return Optional.of(createdDeposit);
    }

    @Override
    public void save(Deposit deposit) {
        depositRepository.save(deposit);
    }
}