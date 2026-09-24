package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.address.AddressResponse;
import com.safeway.tech.domain.models.Address;

public class AddressMapper {

    public static AddressResponse toResponse(Address school) {
        return new AddressResponse(
                school.getId(),
                school.getStreet(),
                school.getNumber(),
                school.getAdditionalDetails(),
                school.getNeighborhood(),
                school.getCity(),
                school.getFederalUnit(),
                school.getZipCode(),
                school.getLatitude(),
                school.getLongitude(),
                school.getType(),
                school.isActive(),
                school.getPrincipal()
        );
    }

}
