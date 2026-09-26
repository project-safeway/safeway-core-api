package com.safeway.tech.auth.core.application;

import java.util.UUID;

public record LoginResult(
        String accessToken,
        long expiresIn,
        String username,
        UUID userId,
        UUID transportId
) {
}

