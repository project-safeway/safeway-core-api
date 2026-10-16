package com.safeway.tech.api.dto.user;

import com.safeway.tech.api.dto.transport.TransportRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(max = 15) String primaryPhoneNumber,
        @Size(max = 15) String secondaryPhoneNumber,
        @Valid TransportRequest transport
) {
}
