package com.safeway.tech.api.dto.route;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteStudentRequest(
        @NotNull UUID studentId,
        @NotNull Integer boardingOrder,
        UUID addressId
) {
}
