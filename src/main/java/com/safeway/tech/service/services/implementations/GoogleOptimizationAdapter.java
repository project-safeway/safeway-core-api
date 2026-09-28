package com.safeway.tech.service.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.safeway.tech.api.dto.route.google.Location;
import com.safeway.tech.api.dto.route.google.RouteMetrics;
import com.safeway.tech.api.dto.route.google.OptimizedStop;
import com.safeway.tech.api.dto.route.google.RouteRequest;
import com.safeway.tech.api.dto.route.google.RouteResponse;
import com.safeway.tech.api.dto.route.google.StopPoint;
import com.safeway.tech.client.GoogleOptimizationClient;
import com.safeway.tech.service.services.IOptimizerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            JsonNode response = client.optimizeRoute(request);
            RouteResponse rawResponse = parseResponse(response, request);

            // Se nao for para otimizar a order, reordena de volta para a order original enviada pelo front
            if (!Boolean.TRUE.equals(request.mustOptimizeOrder())) {
                // Mapa de order original por stopId
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
            throw new RuntimeException("Falha ao otimizar rota com Google: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("MethodLength")
    private RouteResponse parseResponse(JsonNode response, RouteRequest request) {
        List<OptimizedStop> stops = new ArrayList<>();
        List<RouteMetrics> metrics = new ArrayList<>();
        double totalDistance = 0D;
        long totalTime = 0L;

        // Mapa de fallback para coordenadas por label
        Map<String, Location> locationById = new HashMap<>();
        for (StopPoint stopPoint : request.stoppingPoint()) {
            locationById.put(stopPoint.id(), stopPoint.location());
        }

        JsonNode routes = response.path("routes");
        for (JsonNode rota : routes) {
            String vehicleId = rota.path("vehicleLabel").asText("vehicle-1");
            JsonNode visits = rota.path("visits");
            JsonNode transitions = rota.path("transitions");
            for (int i = 0; i < visits.size(); i++) {
                JsonNode visit = visits.get(i);
                JsonNode transition = (transitions != null && i < transitions.size()) ? transitions.get(i) : null;
                String stopId = visit.path("shipmentLabel").asText();

                // Horário de chegada: usar visit.startTime; se ausente, usar transitions[i].endTime
                String arrivalTime = visit.path("startTime").asText("");
                if (arrivalTime == null || arrivalTime.isBlank()) {
                    if (transition != null) {
                        String endTime = transition.path("endTime").asText("");
                        if (endTime != null && !endTime.isBlank()) {
                            arrivalTime = endTime;
                        }
                    }
                }

                double travelDistance = 0D;
                long travelDuration = 0L;
                if (transition != null) {
                    travelDistance = transition.path("travelDistanceMeters").asDouble(0D);
                    travelDuration = parseDuration(transition.path("travelDuration").asText());
                }

                // Localização: tentar arrivalLocation(.latLng), se não houver, usar fallback do request por stopId
                JsonNode arrivalLocation = visit.path("arrivalLocation");
                JsonNode latLngNode = arrivalLocation.has("latLng") ? arrivalLocation.path("latLng") : arrivalLocation;
                boolean hasCoordinates = latLngNode.has("latitude") && latLngNode.has("longitude");
                double lat = hasCoordinates ? latLngNode.path("latitude").asDouble() : Double.NaN;
                double lng = hasCoordinates ? latLngNode.path("longitude").asDouble() : Double.NaN;

                if (Double.isNaN(lat) || Double.isNaN(lng)) {
                    Location fallback = locationById.get(stopId);
                    if (fallback != null) {
                        lat = fallback.lat();
                        lng = fallback.lng();
                    } else {
                        // último recurso: 0,0 (evitar NPEs)
                        lat = 0D;
                        lng = 0D;
                    }
                }

                stops.add(new OptimizedStop(stopId, new Location(lat, lng), arrivalTime, travelDistance, travelDuration));
            }
            JsonNode m = rota.path("metrics");
            double routeDistance = m.path("travelDistanceMeters").asDouble(0D);
            long routeDuration = parseDuration(m.path("travelDuration").asText());
            metrics.add(new RouteMetrics(vehicleId, routeDistance, routeDuration, m.path("performedShipmentCount").asInt(0)));
            totalDistance += routeDistance;
            totalTime += routeDuration;
        }
        return new RouteResponse(totalDistance, totalTime, stops, metrics, "Google");
    }

    private long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return 0L;
        }
        if (duration.endsWith("s")) {
            try {
                return Long.parseLong(duration.substring(0, duration.length() - 1));
            } catch (NumberFormatException ignore) {
            }
        }
        return 0L;
    }

    @Override
    public String providerName() {
        return "Google";
    }
}
