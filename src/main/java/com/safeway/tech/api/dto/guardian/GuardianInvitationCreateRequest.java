package com.safeway.tech.api.dto.guardian;

import com.safeway.tech.domain.enums.InvitationTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record GuardianInvitationCreateRequest(
        @NotNull InvitationTypeEnum type,
        UUID studentId,
        UUID guardianId,
        @Email String targetEmail,
        @Size(max = 25) String targetPhoneNumber
) {
}
