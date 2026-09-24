package com.safeway.tech.api.dto.route.google;

public record MetricasRota(
        String idVeiculo,
        Double distanciaMetros,
        Long duracaoSegundos,
        Integer paradasRealizadas
) {
}
