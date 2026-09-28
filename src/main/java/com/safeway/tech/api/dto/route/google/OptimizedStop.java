package com.safeway.tech.api.dto.route.google;

public record OptimizedStop(
        String stopId,
        Location location,
        String arrivalTime,
        Double travelDistance,
        Long travelDuration
) {
}
