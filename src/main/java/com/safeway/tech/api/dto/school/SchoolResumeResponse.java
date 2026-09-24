package com.safeway.tech.api.dto.school;

import com.safeway.tech.api.dto.address.AddressResponse;
import com.safeway.tech.domain.enums.EducationLevelEnum;

public record SchoolResumeResponse(
        String name,
        EducationLevelEnum educationLevel,
        AddressResponse address
) {
}
