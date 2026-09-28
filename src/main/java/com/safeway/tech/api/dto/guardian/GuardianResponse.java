package com.safeway.tech.api.dto.guardian;

import com.safeway.tech.api.dto.address.AddressResponse;

import java.util.UUID;

public record GuardianResponse(
        UUID id,
        String name,
        String primaryPhoneNumber,
        String secondaryPhoneNumber,
        String email,
        AddressResponse address
) {
}
