package com.safeway.tech.api.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record StudentWithAddress(
        @JsonProperty("studentId") UUID studentId,
        @JsonProperty("studentName") String name,
        @JsonProperty("addressId") UUID addressId,
        String fullAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        @JsonProperty("boardingOrder") Integer ordem
) {
}
