package com.dtaquito_micro_services.user_service.users.interfaces.rest;

import com.dtaquito_micro_services.user_service.users.application.internal.commandservices.EmailService;
import com.dtaquito_micro_services.user_service.users.application.internal.commandservices.PasswordResetTokenService;
import com.dtaquito_micro_services.user_service.users.domain.model.aggregates.User;
import com.dtaquito_micro_services.user_service.users.infrastructure.persistance.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/recover-password")
public class EmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;

    public EmailController(EmailService emailService, UserRepository userRepository, @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder, PasswordResetTokenService passwordResetTokenService) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        passwordResetTokenService.createPasswordResetTokenForUser(email);
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        String email;
        try {
            email = emailService.getEmailFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        User user = userOptional.get();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return ResponseEntity.ok("Password has been reset");
    }
}