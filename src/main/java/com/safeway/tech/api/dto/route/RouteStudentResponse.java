package com.safeway.tech.api.dto.route;

import java.util.UUID;

public record RouteStudentResponse(
        UUID studentId,
        String studentName,
        Integer boardingOrder,
        UUID addressId,
        Integer generalOrder,
        String schoolName,
        String classroom
) {
}
