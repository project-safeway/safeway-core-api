package com.safeway.tech.auth.core.application;

import java.util.UUID;

public record RegisterUserResult(
        UUID userId,
        UUID transportId
) {
}
