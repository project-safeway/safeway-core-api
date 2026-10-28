package com.safeway.tech.auth.infrastructure.entrypoint.dto;

import java.util.UUID;

public record AuthResponseV2(
        String accessToken,
        Long expiresIn,
        String username,
        UUID userId,
        UUID transportId
) {
}
