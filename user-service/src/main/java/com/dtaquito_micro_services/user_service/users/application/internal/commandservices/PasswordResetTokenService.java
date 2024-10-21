package com.dtaquito_micro_services.user_service.users.application.internal.commandservices;

import com.dtaquito_micro_services.user_service.users.domain.model.aggregates.User;
import com.dtaquito_micro_services.user_service.users.infrastructure.persistance.jpa.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordResetTokenService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public PasswordResetTokenService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            String token = emailService.generatePasswordResetToken(email);
            emailService.sendPasswordResetEmail(email, token);
        }
    }
}