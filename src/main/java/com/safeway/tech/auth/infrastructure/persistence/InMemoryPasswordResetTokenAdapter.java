package com.safeway.tech.auth.infrastructure.persistence;

import com.safeway.tech.auth.core.port.PasswordResetTokenPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryPasswordResetTokenAdapter implements PasswordResetTokenPort {

    private static final int MAX_ATTEMPTS = 5;

    private final ConcurrentHashMap<String, ResetEntry> entries = new ConcurrentHashMap<>();

    @Override
    public void storeCode(String normalizedEmail, String codeHash, Instant expiresAt) {
        entries.put(normalizedEmail, new ResetEntry(codeHash, expiresAt, 0));
    }

    @Override
    public boolean verifyAndConsume(String normalizedEmail, String codeHash, Instant now) {
        boolean[] matched = {false};
        entries.compute(normalizedEmail, (key, current) -> {
            if (current == null || now.isAfter(current.expiresAt())) {
                return null;
            }
            if (MessageDigest.isEqual(
                    current.codeHash().getBytes(StandardCharsets.UTF_8),
                    codeHash.getBytes(StandardCharsets.UTF_8)
            )) {
                matched[0] = true;
                return null;
            }
            int nextAttempts = current.attempts() + 1;
            if (nextAttempts >= MAX_ATTEMPTS) {
                return null;
            }
            return new ResetEntry(current.codeHash(), current.expiresAt(), nextAttempts);
        });
        return matched[0];
    }

    private record ResetEntry(String codeHash, Instant expiresAt, int attempts) {
    }
}
