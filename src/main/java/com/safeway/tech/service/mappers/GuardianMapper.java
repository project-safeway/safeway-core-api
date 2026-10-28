package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.guardian.GuardianResponse;
import com.safeway.tech.domain.models.Guardian;

public class GuardianMapper {

    public static GuardianResponse toResponse(Guardian guardian) {
        return new GuardianResponse(
                guardian.getId(),
                guardian.getName(),
                guardian.getPrimaryPhoneNumber(),
                guardian.getSecondaryPhoneNumber(),
                guardian.getEmail(),
                AddressMapper.toResponse(guardian.getAddress())
        );
    }

}
