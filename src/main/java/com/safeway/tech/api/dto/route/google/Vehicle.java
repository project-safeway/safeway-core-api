package com.safeway.tech.api.dto.route.google;

public record Vehicle(
        String id,
        Location intialLocation,
        Location finalLocation
) {
}
