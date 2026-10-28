package com.safeway.tech.api.dto.route.google;

import java.util.List;

public record RouteResponse(
        Double totalDistance,
        Long totalTime,
        List<OptimizedStop> stops,
        List<RouteMetrics> metrics,
        String provider
) {
}
