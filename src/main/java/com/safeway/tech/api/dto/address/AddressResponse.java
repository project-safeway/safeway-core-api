package com.safeway.tech.api.dto.address;

import java.math.BigDecimal;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        String street,
        String number,
        String additionalDetails,
        String neighborhood,
        String city,
        String federalUnit,
        String zipCode,
        BigDecimal latitude,
        BigDecimal longitude,
        String type,
        Boolean active,
        Boolean principal
) {
}
