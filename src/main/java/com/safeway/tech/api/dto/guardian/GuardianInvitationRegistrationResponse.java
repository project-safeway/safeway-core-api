package com.safeway.tech.api.dto.guardian;

import com.safeway.tech.domain.enums.InvitationStatusEnum;

import java.util.UUID;

public record GuardianInvitationRegistrationResponse(
        UUID guardianId,
        InvitationStatusEnum status,
        boolean requiresApproval
) {
}
