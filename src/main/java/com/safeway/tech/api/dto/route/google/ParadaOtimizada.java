package com.safeway.tech.api.dto.route.google;

public record ParadaOtimizada(
        String idParada,
        Localizacao localizacao,
        String horarioChegada,
        Double distanciaAteAqui,
        Long tempoViagem
) {
}
