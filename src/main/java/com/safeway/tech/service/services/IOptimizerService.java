package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.google.RotasRequest;
import com.safeway.tech.api.dto.route.google.RouteResponse;

public interface IOptimizerService {
    RouteResponse optimizeRoute(RotasRequest request);
    String providerName();
}
