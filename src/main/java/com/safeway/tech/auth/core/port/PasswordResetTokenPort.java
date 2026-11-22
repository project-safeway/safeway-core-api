package com.safeway.tech.auth.core.port;

import java.time.Instant;

public interface PasswordResetTokenPort {

    void storeCode(String normalizedEmail, String codeHash, Instant expiresAt);

    boolean verifyAndConsume(String normalizedEmail, String codeHash, Instant now);
}
