package com.dtaquito_backend.dtaquito_backend.users.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.PasswordResetToken;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.PasswordResetTokenRepository;
import com.dtaquito_backend.dtaquito_backend.users.infrastructure.persistance.jpa.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, UserRepository userRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createPasswordResetTokenForUser(String email, String token) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Eliminar tokens existentes para este usuario
            passwordResetTokenRepository.deleteByUser(user);

            // Crear un nuevo token
            PasswordResetToken myToken = new PasswordResetToken(token, user, calculateExpiryDate(24 * 60)); // 24 hours
            passwordResetTokenRepository.save(myToken);
        }
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}