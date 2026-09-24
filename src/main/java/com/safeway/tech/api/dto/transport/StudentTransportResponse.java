package com.safeway.tech.api.dto.transport;

import java.util.UUID;

public record StudentTransportResponse(
        UUID studentId,
        String name,
        String school
) {
}
