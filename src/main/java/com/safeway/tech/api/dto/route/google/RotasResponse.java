package com.safeway.tech.api.dto.route.google;

import java.util.List;

public record RotasResponse(
        Double distanciaTotal,
        Long tempoTotal,
        List<ParadaOtimizada> paradas,
        List<MetricasRota> metricas,
        String provedor
) {
}
