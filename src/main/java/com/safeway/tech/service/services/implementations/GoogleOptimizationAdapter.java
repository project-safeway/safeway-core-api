package com.safeway.tech.service.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.safeway.tech.api.dto.route.google.Location;
import com.safeway.tech.api.dto.route.google.RouteMetrics;
import com.safeway.tech.api.dto.route.google.OptimizedStop;
import com.safeway.tech.api.dto.route.google.RouteRequest;
import com.safeway.tech.api.dto.route.google.RouteResponse;
import com.safeway.tech.api.dto.route.google.StopPoint;
import com.safeway.tech.api.dto.route.google.Vehicle;
import com.safeway.tech.client.GoogleOptimizationClient;
import com.safeway.tech.service.services.IOptimizerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Service("googleOptimization")
public class GoogleOptimizationAdapter implements IOptimizerService {

    private final GoogleOptimizationClient client;

    public GoogleOptimizationAdapter(GoogleOptimizationClient client) {
        this.client = client;
    }

    @Override
    @SuppressWarnings("MethodLength")
    public RouteResponse optimizeRoute(RouteRequest request) {
        try {
            validateRequest(request);
            JsonNode response = client.optimizeRoute(request);
            RouteResponse rawResponse = parseResponse(response, request);

            // Se nao for para otimizar a order, reordena de volta para a order original enviada pelo front
            if (!Boolean.TRUE.equals(request.mustOptimizeOrder())) {
                // Map original order by stopId.
                Map<String, Integer> originalOrder = new HashMap<>();
                int idx = 0;
                for (StopPoint stopPoint : request.stoppingPoint()) {
                    // se o request tiver campo order, use-o; caso contrario, use o indice
                    int order = stopPoint.order() != null ? stopPoint.order() : idx;
                    originalOrder.put(stopPoint.id(), order);
                    idx++;
                }

                List<OptimizedStop> reordered = new ArrayList<>(rawResponse.stops());
                reordered.sort(Comparator.comparingInt(optimizedStop ->
                        originalOrder.getOrDefault(optimizedStop.stopId(), Integer.MAX_VALUE)));

                return new RouteResponse(
                        rawResponse.totalDistance(),
                        rawResponse.totalTime(),
                        reordered,
                        rawResponse.metrics(),
                        rawResponse.provider()
                );
            }

            return rawResponse;
        } catch (Exception e) {
            throw new RuntimeException("Failed to optimize route with Google: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("MethodLength")
    private RouteResponse parseResponse(JsonNode response, RouteRequest request) {
        List<OptimizedStop> stops = new ArrayList<>();
        List<RouteMetrics> metrics = new ArrayList<>();
        double totalDistance = 0D;
        long totalTime = 0L;

        if (response == null || response.isNull()) {
            throw new IllegalStateException("Google optimization returned an empty response");
        }

        // Fallback map of coordinates by label.
        Map<String, Location> locationById = new HashMap<>();
        for (StopPoint stopPoint : request.stoppingPoint()) {
            locationById.put(stopPoint.id(), stopPoint.location());
        }

        JsonNode routes = response.path("routes");
        if (!routes.isArray() || routes.isEmpty()) {
            throw new IllegalStateException("Google optimization returned no routes");
        }
        Set<String> optimizedStopIds = new HashSet<>();
        for (JsonNode rota : routes) {
            String vehicleId = rota.path("vehicleLabel").asText("vehicle-1");
            JsonNode visits = rota.path("visits");
            JsonNode transitions = rota.path("transitions");
            JsonNode routeMetrics = rota.path("metrics");
            if (!visits.isArray() || !transitions.isArray() || routeMetrics.isMissingNode()) {
                throw new IllegalStateException("Google optimization returned incomplete route data");
            }
            for (int i = 0; i < visits.size(); i++) {
                JsonNode visit = visits.get(i);
                if (i >= transitions.size()) {
                    throw new IllegalStateException("Google optimization omitted a stop transition");
                }
                JsonNode transition = transitions.get(i);
                String stopId = visit.path("shipmentLabel").asText();
                if (stopId.isBlank() || !locationById.containsKey(stopId) || !optimizedStopIds.add(stopId)) {
                    throw new IllegalStateException("Google optimization returned an unknown or duplicate stop");
                }

                // Use visit.startTime for arrival; fall back to transitions[i].endTime if absent.
                String arrivalTime = visit.path("startTime").asText("");
                if (arrivalTime == null || arrivalTime.isBlank()) {
                    if (transition != null) {
                        String endTime = transition.path("endTime").asText("");
                        if (endTime != null && !endTime.isBlank()) {
                            arrivalTime = endTime;
                        }
                    }
                }

                double travelDistance = requiredNonNegativeDouble(transition, "travelDistanceMeters");
                long travelDuration = parseDuration(transition.path("travelDuration").asText());

                // Try arrivalLocation(.latLng); otherwise use the request fallback for this stopId.
                JsonNode arrivalLocation = visit.path("arrivalLocation");
                JsonNode latLngNode = arrivalLocation.has("latLng") ? arrivalLocation.path("latLng") : arrivalLocation;
                boolean hasCoordinates = latLngNode.hasNonNull("latitude") && latLngNode.hasNonNull("longitude");
                double lat = hasCoordinates ? latLngNode.path("latitude").asDouble(Double.NaN) : Double.NaN;
                double lng = hasCoordinates ? latLngNode.path("longitude").asDouble(Double.NaN) : Double.NaN;

                if (!isValidCoordinate(lat, lng)) {
                    Location fallback = locationById.get(stopId);
                    if (fallback == null || !isValidCoordinate(fallback.lat(), fallback.lng())) {
                        throw new IllegalStateException("Google optimization returned invalid stop coordinates");
                    }
                    lat = fallback.lat();
                    lng = fallback.lng();
                }

                stops.add(new OptimizedStop(stopId, new Location(lat, lng), arrivalTime, travelDistance, travelDuration));
            }
            double routeDistance = requiredNonNegativeDouble(routeMetrics, "travelDistanceMeters");
            long routeDuration = parseDuration(routeMetrics.path("travelDuration").asText());
            JsonNode performedShipmentCount = routeMetrics.path("performedShipmentCount");
            if (!performedShipmentCount.canConvertToInt() || performedShipmentCount.asInt() < 0) {
                throw new IllegalStateException("Google optimization returned invalid performed stop count");
            }
            metrics.add(new RouteMetrics(
                    vehicleId, routeDistance, routeDuration, performedShipmentCount.asInt()));
            totalDistance += routeDistance;
            totalTime += routeDuration;
        }
        if (optimizedStopIds.size() != locationById.size()) {
            throw new IllegalStateException("Google optimization did not return every requested stop");
        }
        return new RouteResponse(totalDistance, totalTime, stops, metrics, "Google");
    }

    private long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            throw new IllegalStateException("Google optimization returned no duration");
        }
        if (duration.endsWith("s")) {
            try {
                long seconds = Long.parseLong(duration.substring(0, duration.length() - 1));
                if (seconds < 0) {
                    throw new IllegalStateException("Google optimization returned a negative duration");
                }
                return seconds;
            } catch (NumberFormatException exception) {
                throw new IllegalStateException("Google optimization returned an invalid duration", exception);
            }
        }
        throw new IllegalStateException("Google optimization returned an invalid duration");
    }

    private void validateRequest(RouteRequest request) {
        if (request == null || request.stoppingPoint() == null || request.stoppingPoint().isEmpty()) {
            throw new IllegalArgumentException("At least one route stop is required");
        }
        Vehicle vehicle = request.vehicle();
        if (vehicle == null || vehicle.id() == null || vehicle.id().isBlank()
                || vehicle.intialLocation() == null || vehicle.finalLocation() == null
                || !isValidCoordinate(vehicle.intialLocation().lat(), vehicle.intialLocation().lng())
                || !isValidCoordinate(vehicle.finalLocation().lat(), vehicle.finalLocation().lng())) {
            throw new IllegalArgumentException("A vehicle with valid start and end coordinates is required");
        }
        Set<String> stopIds = new HashSet<>();
        for (StopPoint stop : request.stoppingPoint()) {
            if (stop == null || stop.id() == null || stop.id().isBlank()
                    || !stopIds.add(stop.id())
                    || stop.location() == null
                    || !isValidCoordinate(stop.location().lat(), stop.location().lng())) {
                throw new IllegalArgumentException("Route stops require unique ids and valid coordinates");
            }
        }
    }

    private double requiredNonNegativeDouble(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isNumber() || !Double.isFinite(value.asDouble()) || value.asDouble() < 0) {
            throw new IllegalStateException("Google optimization returned invalid " + field);
        }
        return value.asDouble();
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }

    @Override
    public String providerName() {
        return "Google";
    }
}
