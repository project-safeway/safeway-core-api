package com.safeway.tech.api.dto.guardian;

import java.util.UUID;

public record GuardianCandidateResponse(
        UUID guardianId,
        String name,
        String primaryPhoneNumber,
        String email
) {
}
