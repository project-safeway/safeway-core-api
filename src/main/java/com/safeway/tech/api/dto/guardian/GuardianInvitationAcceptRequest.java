package com.safeway.tech.api.dto.guardian;

import com.safeway.tech.api.dto.address.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuardianInvitationAcceptRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 100) String name,
        @Size(max = 25) String primaryPhoneNumber,
        @Size(max = 25) String secondaryPhoneNumber,
        @Valid AddressRequest address
) {
}
