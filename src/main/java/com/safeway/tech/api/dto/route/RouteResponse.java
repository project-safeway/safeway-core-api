package com.safeway.tech.api.dto.route;

import com.safeway.tech.domain.enums.RouteTypeEnum;

import java.sql.Time;
import java.util.UUID;

public record RouteResponse(
        UUID id,
        String name,
        Time startTime,
        Time endTime,
        RouteTypeEnum routeType,
        Boolean active
) {
}
