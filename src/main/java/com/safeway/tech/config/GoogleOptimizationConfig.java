package com.safeway.tech.config;

import com.google.maps.GeoApiContext;
import com.safeway.tech.client.GoogleOptimizationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GoogleOptimizationConfig {
    @Value("${google.projectId:project-id-placeholder}")
    private String projectId;

    @Value("${google.maps.api.key:api-key-placeholder}")
    private String apiKey;

    @Bean
    public GoogleOptimizationClient googleOptimizationClient(WebClient.Builder builder) {
        return new GoogleOptimizationClient(builder, projectId);
    }

    @Bean
    public GeoApiContext geoApiContext() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("${google.maps.api.key}")) {
            throw new IllegalStateException("Google Maps API key is not configured correctly in application.properties!");
        }

        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }
}
