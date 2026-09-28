package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.route.RouteResponse;
import com.safeway.tech.domain.models.Route;

public class RouteMapper {

    public static RouteResponse toResponse(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getName(),
                route.getStartTime(),
                route.getEndTime(),
                route.getRouteType(),
                route.isActive()
        );
    }
}
