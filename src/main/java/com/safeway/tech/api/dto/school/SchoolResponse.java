package com.safeway.tech.api.dto.school;

import com.safeway.tech.api.dto.address.AddressResponse;
import com.safeway.tech.domain.enums.EducationLevelEnum;

import java.util.UUID;

public record SchoolResponse(
        UUID id,
        String name,
        EducationLevelEnum educationLevel,
        AddressResponse address
) {
}
