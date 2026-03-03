package hu.tamas.szakdolgozatjava.service;

import hu.tamas.szakdolgozatjava.model.PasswordResetToken;
import hu.tamas.szakdolgozatjava.model.User;
import hu.tamas.szakdolgozatjava.repository.PasswordResetTokenRepository;
import hu.tamas.szakdolgozatjava.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int TOKEN_MINUTES = 30;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void requestReset(String email, String baseUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expires = LocalDateTime.now().plusMinutes(TOKEN_MINUTES);

        tokenRepository.save(new PasswordResetToken(token, user, expires));

        String link = baseUrl + "/reset-password?token=" + token;

        String subject = "Jelszó helyreállítás";
        String body =
                "Szia!\n\n" +
                        "Kattints az alábbi linkre az új jelszó beállításához:\n" + link + "\n\n" +
                        "A link " + TOKEN_MINUTES + " percig érvényes.\n" +
                        "Ha nem te kérted, hagyd figyelmen kívül ezt az e-mailt.";

        emailService.send(email, subject, body);
    }

    public PasswordResetToken validateToken(String token) {
        PasswordResetToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Érvénytelen token."));

        if (t.isUsed()) {
            throw new IllegalArgumentException("A token már fel lett használva.");
        }
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A token lejárt.");
        }
        return t;
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken t = validateToken(token);

        User user = t.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        t.setUsed(true);
        tokenRepository.save(t);
    }
}