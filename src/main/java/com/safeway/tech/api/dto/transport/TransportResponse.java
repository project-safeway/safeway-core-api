package com.safeway.tech.api.dto.transport;

import java.util.UUID;

public record TransportResponse(
        UUID transportId,
        String licensePlate,
        String model,
        Integer capacity
) {
}
