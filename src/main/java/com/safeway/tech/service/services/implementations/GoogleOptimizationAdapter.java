package com.safeway.tech.service.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.safeway.tech.client.GoogleOptimizationClient;
import com.safeway.tech.api.dto.route.google.Localizacao;
import com.safeway.tech.api.dto.route.google.MetricasRota;
import com.safeway.tech.api.dto.route.google.ParadaOtimizada;
import com.safeway.tech.api.dto.route.google.RotasRequest;
import com.safeway.tech.api.dto.route.google.RotasResponse;
import com.safeway.tech.service.services.IOptimizerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("googleOptimization")
public class GoogleOptimizationAdapter implements IOptimizerService {

    private final GoogleOptimizationClient cliente;

    public GoogleOptimizationAdapter(GoogleOptimizationClient cliente) {
        this.cliente = cliente;
    }

    @Override
    @SuppressWarnings("MethodLength")
    public RotasResponse otimizarRota(RotasRequest request) {
        try {
            JsonNode response = cliente.otimizarRotas(request);
            RotasResponse bruto = parseResposta(response, request);

            // Se nao for para otimizar a ordem, reordena de volta para a ordem original enviada pelo front
            if (!Boolean.TRUE.equals(request.otimizarOrdem())) {
                // Mapa de ordem original por idParada
                Map<String, Integer> ordemOriginal = new HashMap<>();
                int idx = 0;
                for (var p : request.pontosParada()) {
                    // se o request tiver campo ordem, use-o; caso contrario, use o indice
                    int ordem = p.ordem() != null ? p.ordem() : idx;
                    ordemOriginal.put(p.id(), ordem);
                    idx++;
                }

                List<ParadaOtimizada> reordenadas = new ArrayList<>(bruto.paradas());
                reordenadas.sort(Comparator.comparingInt(p -> ordemOriginal.getOrDefault(p.idParada(), Integer.MAX_VALUE)));

                return new RotasResponse(
                        bruto.distanciaTotal(),
                        bruto.tempoTotal(),
                        reordenadas,
                        bruto.metricas(),
                        bruto.provedor()
                );
            }

            return bruto;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao otimizar rota com Google: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("MethodLength")
    private RotasResponse parseResposta(JsonNode response, RotasRequest request) {
        List<ParadaOtimizada> paradas = new ArrayList<>();
        List<MetricasRota> metricas = new ArrayList<>();
        double distanciaTotal = 0D;
        long tempoTotal = 0L;

        // Mapa de fallback para coordenadas por label
        Map<String, Localizacao> locaisPorId = new HashMap<>();
        for (var p : request.pontosParada()) {
            locaisPorId.put(p.id(), p.localizacao());
        }

        JsonNode routes = response.path("routes");
        for (JsonNode rota : routes) {
            String idVeiculo = rota.path("vehicleLabel").asText("vehicle-1");
            JsonNode visits = rota.path("visits");
            JsonNode transitions = rota.path("transitions");
            for (int i = 0; i < visits.size(); i++) {
                JsonNode visit = visits.get(i);
                JsonNode transition = (transitions != null && i < transitions.size()) ? transitions.get(i) : null;
                String idParada = visit.path("shipmentLabel").asText();

                // Horário de chegada: usar visit.startTime; se ausente, usar transitions[i].endTime
                String horarioChegada = visit.path("startTime").asText("");
                if (horarioChegada == null || horarioChegada.isBlank()) {
                    if (transition != null) {
                        String endTime = transition.path("endTime").asText("");
                        if (endTime != null && !endTime.isBlank()) {
                            horarioChegada = endTime;
                        }
                    }
                }

                double distanciaAteAqui = 0D;
                long tempoViagem = 0L;
                if (transition != null) {
                    distanciaAteAqui = transition.path("travelDistanceMeters").asDouble(0D);
                    tempoViagem = parseDuracao(transition.path("travelDuration").asText());
                }

                // Localização: tentar arrivalLocation(.latLng), se não houver, usar fallback do request por idParada
                JsonNode arrivalLocation = visit.path("arrivalLocation");
                JsonNode latLngNode = arrivalLocation.has("latLng") ? arrivalLocation.path("latLng") : arrivalLocation;
                boolean temCoordsResposta = latLngNode.has("latitude") && latLngNode.has("longitude");
                double lat = temCoordsResposta ? latLngNode.path("latitude").asDouble() : Double.NaN;
                double lng = temCoordsResposta ? latLngNode.path("longitude").asDouble() : Double.NaN;

                if (Double.isNaN(lat) || Double.isNaN(lng)) {
                    Localizacao fallback = locaisPorId.get(idParada);
                    if (fallback != null) {
                        lat = fallback.lat();
                        lng = fallback.lng();
                    } else {
                        // último recurso: 0,0 (evitar NPEs)
                        lat = 0D;
                        lng = 0D;
                    }
                }

                paradas.add(new ParadaOtimizada(idParada, new Localizacao(lat, lng), horarioChegada, distanciaAteAqui, tempoViagem));
            }
            JsonNode m = rota.path("metrics");
            double distRota = m.path("travelDistanceMeters").asDouble(0D);
            long durRota = parseDuracao(m.path("travelDuration").asText());
            metricas.add(new MetricasRota(idVeiculo, distRota, durRota, m.path("performedShipmentCount").asInt(0)));
            distanciaTotal += distRota;
            tempoTotal += durRota;
        }
        return new RotasResponse(distanciaTotal, tempoTotal, paradas, metricas, "Google");
    }

    private long parseDuracao(String dur) {
        if (dur == null || dur.isEmpty()) {
            return 0L;
        }
        if (dur.endsWith("s")) {
            try {
                return Long.parseLong(dur.substring(0, dur.length() - 1));
            } catch (NumberFormatException ignore) {
            }
        }
        return 0L;
    }

    @Override
    public String nomeProvedor() {
        return "Google";
    }
}
