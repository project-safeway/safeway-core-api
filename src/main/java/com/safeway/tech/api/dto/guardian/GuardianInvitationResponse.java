package com.safeway.tech.api.dto.guardian;

import com.safeway.tech.domain.enums.InvitationStatusEnum;
import com.safeway.tech.domain.enums.InvitationTypeEnum;

import java.time.Instant;
import java.util.UUID;

public record GuardianInvitationResponse(
        UUID id,
        InvitationTypeEnum type,
        InvitationStatusEnum status,
        Instant expiresAt,
        String studentName
) {
}
