package com.safeway.tech.api.dto.guardian;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record GuardianInvitationApprovalRequest(
        @NotEmpty List<@NotNull UUID> studentIds,
        UUID preRegistrationGuardianId
) {
}
