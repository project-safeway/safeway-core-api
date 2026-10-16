package com.safeway.tech.api.dto.guardian;

import com.safeway.tech.domain.enums.InvitationTypeEnum;

import java.time.Instant;
import java.util.UUID;

public record GuardianInvitationCreatedResponse(
        UUID id,
        InvitationTypeEnum type,
        String token,
        Instant expiresAt
) {
}
