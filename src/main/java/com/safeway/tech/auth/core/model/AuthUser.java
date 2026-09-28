package com.safeway.tech.auth.core.model;

import java.util.UUID;

public record AuthUser(
        UUID id,
        String name,
        String email,
        String passwordHash,
        String role,
        UUID transportId
) {
}
