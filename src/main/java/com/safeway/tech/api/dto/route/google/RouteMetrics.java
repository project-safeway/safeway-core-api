package com.safeway.tech.api.dto.route.google;

public record RouteMetrics(
        String vehicleId,
        Double distanceInMeters,
        Long durationInSeconds,
        Integer stopsMade
) {
}
