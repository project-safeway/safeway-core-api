package com.safeway.tech.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auth.oauth2.GoogleCredentials;
import com.safeway.tech.api.dto.route.google.Location;
import com.safeway.tech.api.dto.route.google.RouteRequest;
import com.safeway.tech.api.dto.route.google.StopPoint;
import com.safeway.tech.api.dto.route.google.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Component
@Slf4j
public class GoogleOptimizationClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String projectId;
    private GoogleCredentials credentials;

    public GoogleOptimizationClient(WebClient.Builder builder,
                                    @Value("${google.projectId:dev-local}") String configuredProjectId) {
        this.webClient = builder.baseUrl("https://routeoptimization.googleapis.com").build();
        this.projectId = resolveProjectId(configuredProjectId);
        try {
            this.credentials = initCredentials();
        } catch (IOException e) {
            log.warn("Unable to initialize Google credentials. Details: {}", e.getMessage());
            this.credentials = null;
        }
    }

    private String resolveProjectId(String fallback) {
        String keyPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (keyPath != null && !keyPath.isBlank()) {
            try {
                String content = Files.readString(Path.of(keyPath));
                JsonNode node = mapper.readTree(content);
                String pid = node.path("project_id").asText();
                if (pid != null && !pid.isBlank()) {
                    log.info("projectId extracted from service account: {}", pid);
                    return pid;
                }
                log.info("Service account has no project_id; using configured value: {}", fallback);
            } catch (Exception ex) {
                log.error("Failed to read project_id from file: {}", ex.getMessage());
            }
        } else {
            log.info("GOOGLE_APPLICATION_CREDENTIALS is not set; using configured projectId: {}", fallback);
        }
        return fallback;
    }

    private GoogleCredentials initCredentials() throws IOException {
        String keyPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (keyPath != null && !keyPath.isBlank()) {
            try (FileInputStream in = new FileInputStream(keyPath)) {
                return GoogleCredentials.fromStream(in).createScoped("https://www.googleapis.com/auth/cloud-platform");
            }
        }
        return GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    public JsonNode optimizeRoute(RouteRequest request) {
        if (projectId == null || projectId.isBlank() || "dev-local".equals(projectId)) {
            throw new RuntimeException("Invalid projectId. Set 'google.projectId' or GOOGLE_APPLICATION_CREDENTIALS with project_id.");
        }
        ObjectNode body = buildRequest(request);
        log.info("Google optimizeTours body: {}", body);
        String uri = String.format("/v1/projects/%s:optimizeTours", projectId);
        try {
            return webClient.post()
                    .uri(uri)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(30));
        } catch (WebClientResponseException wcre) {
            throw new RuntimeException("Google API error: " + wcre.getResponseBodyAsString(), wcre);
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while calling Google route optimization: " + ex.getMessage(), ex);
        }
    }

    private ObjectNode buildRequest(RouteRequest request) {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode model = mapper.createObjectNode();

        ArrayNode shipments = mapper.createArrayNode();
        for (StopPoint p : request.stoppingPoint()) {
            ObjectNode shipment = mapper.createObjectNode();
            shipment.put("label", p.id());
            ArrayNode deliveries = mapper.createArrayNode();
            ObjectNode delivery = mapper.createObjectNode();
            delivery.set("arrivalLocation", createLocation(p.location()));
            deliveries.add(delivery);
            shipment.set("deliveries", deliveries);
            shipments.add(shipment);
        }
        model.set("shipments", shipments);

        // Vehicles
        ArrayNode vehicles = mapper.createArrayNode();
        Vehicle v = request.vehicle();
        ObjectNode vehicle = mapper.createObjectNode();
        vehicle.put("label", v.id());
        vehicle.set("startLocation", createLocation(v.intialLocation()));
        vehicle.set("endLocation", createLocation(v.finalLocation()));
        vehicles.add(vehicle);
        model.set("vehicles", vehicles);

        root.set("model", model);
        return root;
    }

    private ObjectNode createLocation(Location loc) {
        if (loc == null) {
            throw new RuntimeException("Location is missing");
        }
        double lat = loc.lat();
        double lng = loc.lng();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new RuntimeException("Invalid coordinates: " + lat + ", " + lng);
        }
        // The API rejects nested latLng values, so send latitude and longitude directly.
        ObjectNode location = mapper.createObjectNode();
        location.put("latitude", lat);
        location.put("longitude", lng);
        return location;
    }

    private String getAccessToken() {
        try {
            if (credentials != null) {
                credentials.refreshIfExpired();
                return credentials.getAccessToken().getTokenValue();
            }
            String env = System.getenv("GOOGLE_ACCESS_TOKEN");
            if (env != null && !env.isBlank()) {
                return env;
            }
            throw new RuntimeException("Google credentials are missing. Set GOOGLE_APPLICATION_CREDENTIALS or GOOGLE_ACCESS_TOKEN.");
        } catch (IOException e) {
            String env = System.getenv("GOOGLE_ACCESS_TOKEN");
            if (env != null && !env.isBlank()) {
                return env;
            }
            throw new RuntimeException("Failed to obtain Google token: " + e.getMessage(), e);
        }
    }
}
