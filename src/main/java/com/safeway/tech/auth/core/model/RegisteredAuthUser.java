package com.safeway.tech.auth.core.model;

import java.util.UUID;

public record RegisteredAuthUser(
        UUID userId,
        UUID transportId
) {
}
