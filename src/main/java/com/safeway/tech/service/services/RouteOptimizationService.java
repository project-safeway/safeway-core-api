package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.google.RouteRequest;
import com.safeway.tech.api.dto.route.google.RouteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class RouteOptimizationService {

    private final List<IOptimizerService> providers;

    public RouteOptimizationService(List<IOptimizerService> providers) {
        this.providers = providers;
        if (providers != null && !providers.isEmpty()) {
            log.info("RouteOptimizationService initialized with {} provider(s)", providers.size());
            providers.forEach(p -> System.out.println("  - " + p.providerName()));
        } else {
            log.warn("⚠️ WARNING: No route providers were found!");
        }
    }

    public RouteResponse optimizeBestRoute(RouteRequest request) {
        if (providers == null || providers.isEmpty()) {
            throw new RuntimeException(
                    "No route providers are available. " +
                            "Verify that GoogleOptimizationAdapter is configured as a @Service"
            );
        }

        return providers.stream()
                .map(provider -> {
                    try {
                        log.info("Calling provider: {}", provider.providerName());
                        long startTime = System.currentTimeMillis();
                        RouteResponse response = provider.optimizeRoute(request);
                        long endTime = System.currentTimeMillis();
                        log.info("Provider {} responded in {}ms", provider.providerName(), endTime - startTime);
                        return response;
                    } catch (Exception e) {
                        log.error("Provider {} failed: {}", provider.providerName(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(RouteResponse::totalDistance))
                .orElseThrow(() -> new RuntimeException("No route is available from any provider"));
    }
}
