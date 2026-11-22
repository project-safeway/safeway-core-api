package com.safeway.tech.auth.core.application;

import com.safeway.tech.auth.core.exception.InvalidPasswordResetCodeException;
import com.safeway.tech.auth.core.model.AuthUser;
import com.safeway.tech.auth.core.port.LoadAuthUserPort;
import com.safeway.tech.auth.core.port.PasswordHasherPort;
import com.safeway.tech.auth.core.port.PasswordResetNotifierPort;
import com.safeway.tech.auth.core.port.PasswordResetTokenPort;
import com.safeway.tech.auth.core.port.UpdateUserPasswordPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

public class PasswordResetUseCase {

    private static final Duration CODE_TTL = Duration.ofMinutes(15);
    private static final int CODE_BOUND = 1_000_000;

    private final LoadAuthUserPort loadAuthUserPort;
    private final PasswordResetTokenPort tokenPort;
    private final PasswordResetNotifierPort notifierPort;
    private final PasswordHasherPort passwordHasherPort;
    private final UpdateUserPasswordPort updateUserPasswordPort;
    private final SecureRandom secureRandom;
    private final Clock clock;

    public PasswordResetUseCase(
            LoadAuthUserPort loadAuthUserPort,
            PasswordResetTokenPort tokenPort,
            PasswordResetNotifierPort notifierPort,
            PasswordHasherPort passwordHasherPort,
            UpdateUserPasswordPort updateUserPasswordPort
    ) {
        this(
                loadAuthUserPort,
                tokenPort,
                notifierPort,
                passwordHasherPort,
                updateUserPasswordPort,
                new SecureRandom(),
                Clock.systemUTC()
        );
    }

    public PasswordResetUseCase(
            LoadAuthUserPort loadAuthUserPort,
            PasswordResetTokenPort tokenPort,
            PasswordResetNotifierPort notifierPort,
            PasswordHasherPort passwordHasherPort,
            UpdateUserPasswordPort updateUserPasswordPort,
            SecureRandom secureRandom,
            Clock clock
    ) {
        this.loadAuthUserPort = loadAuthUserPort;
        this.tokenPort = tokenPort;
        this.notifierPort = notifierPort;
        this.passwordHasherPort = passwordHasherPort;
        this.updateUserPasswordPort = updateUserPasswordPort;
        this.secureRandom = secureRandom;
        this.clock = clock;
    }

    public void requestReset(String email) {
        String normalized = normalizeEmail(email);
        Optional<AuthUser> user = loadAuthUserPort.findByEmail(normalized);
        if (user.isEmpty()) {
            return;
        }

        String code = String.format("%06d", secureRandom.nextInt(CODE_BOUND));
        Instant expiresAt = clock.instant().plus(CODE_TTL);
        notifierPort.notifyCode(normalized, code, CODE_TTL.toMinutes());
        tokenPort.storeCode(normalized, hashCode(code), expiresAt);
    }

    public void resetPassword(String email, String code, String newPassword) {
        String normalized = normalizeEmail(email);
        String cleanCode = code == null ? "" : code.trim();
        if (!tokenPort.verifyAndConsume(normalized, hashCode(cleanCode), clock.instant())) {
            throw new InvalidPasswordResetCodeException();
        }

        AuthUser user = loadAuthUserPort.findByEmail(normalized)
                .orElseThrow(InvalidPasswordResetCodeException::new);

        String passwordHash = passwordHasherPort.hash(newPassword);
        updateUserPasswordPort.updatePasswordHash(user.id(), passwordHash);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String hashCode(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
