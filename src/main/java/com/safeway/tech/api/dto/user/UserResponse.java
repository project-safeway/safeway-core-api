package com.safeway.tech.api.dto.user;

import com.safeway.tech.api.dto.transport.TransportResponse;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String primaryPhoneNumber,
        String secondaryPhoneNumber,
        TransportResponse transport,
        String role
) {
}
