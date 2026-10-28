package com.safeway.tech.api.dto.transport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransportRequest(
        @NotBlank @Size(max = 10) String licensePlate,
        @Size(max = 50) String model,
        @PositiveOrZero Integer capacity) {
}
