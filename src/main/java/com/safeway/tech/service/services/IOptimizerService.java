package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.google.RouteRequest;
import com.safeway.tech.api.dto.route.google.RouteResponse;

public interface IOptimizerService {
    RouteResponse optimizeRoute(RouteRequest request);
    String providerName();
}
