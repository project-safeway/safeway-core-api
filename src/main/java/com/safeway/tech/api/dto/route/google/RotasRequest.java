package com.safeway.tech.api.dto.route.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RotasRequest(
        Vehicle vehicle,
        List<StopPoint> stoppingPoint,
        Boolean mustOptimizeOrder
) {
}
