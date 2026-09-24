package com.safeway.tech.api.dto.attendance;

import com.safeway.tech.api.dto.route.RouteResponse;
import com.safeway.tech.domain.enums.AttendanceStatusEnum;

import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        RouteResponse route,
        AttendanceStatusEnum status
) {
}
