package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.address.AddressResponse;
import com.safeway.tech.domain.models.Address;

public class AddressMapper {

    public static AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getNumber(),
                address.getAdditionalDetails(),
                address.getNeighborhood(),
                address.getCity(),
                address.getFederalUnit(),
                address.getZipCode(),
                address.getLatitude(),
                address.getLongitude(),
                address.getType(),
                address.isActive(),
                address.getPrincipal()
        );
    }

}
