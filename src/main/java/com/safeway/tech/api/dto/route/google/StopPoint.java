package com.safeway.tech.api.dto.route.google;

public record StopPoint(
        String id,
        Location location,
        Integer order // order opcional enviada pelo front; pode ser null
) {
}
