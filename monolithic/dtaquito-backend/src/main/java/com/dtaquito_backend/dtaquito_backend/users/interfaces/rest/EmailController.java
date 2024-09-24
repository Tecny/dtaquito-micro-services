package com.dtaquito_backend.dtaquito_backend.users.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.users.application.internal.commandservices.EmailService;
import com.dtaquito_backend.dtaquito_backend.users.application.internal.commandservices.PasswordResetTokenService;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.PasswordResetToken;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.PasswordResetTokenRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recover-password")
public class EmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService passwordResetTokenService;

    public EmailController(EmailService emailService, UserRepository userRepository, PasswordResetTokenRepository tokenRepository, @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder, PasswordResetTokenService passwordResetTokenService) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createPasswordResetTokenForUser(email, token);

        emailService.sendPasswordResetEmail(email, token);
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        PasswordResetToken passwordResetToken = tokenRepository.findByToken(token);
        if (passwordResetToken == null) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        User user = passwordResetToken.getUser();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Eliminar el token después de usarlo
        tokenRepository.delete(passwordResetToken);

        return ResponseEntity.ok("Password has been reset");
    }
}