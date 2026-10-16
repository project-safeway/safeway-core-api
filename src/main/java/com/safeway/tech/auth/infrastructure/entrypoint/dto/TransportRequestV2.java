package com.safeway.tech.auth.infrastructure.entrypoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TransportRequestV2(
        @NotBlank(message = "License plate is required")
        @Size(max = 10)
        String licensePlate,
        @Size(max = 50)
        String model,
        @PositiveOrZero
        Integer capacity
) {
}
