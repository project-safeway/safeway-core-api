package com.safeway.tech.api.dto.guardian;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PendingGuardianInvitationResponse(
        UUID invitationId,
        GuardianResponse guardian,
        Instant createdAt,
        List<GuardianCandidateResponse> possiblePreRegistrations
) {
}
