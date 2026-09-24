package com.safeway.tech.api.dto.route.google;

public record PontoParada(
        String id,
        Localizacao localizacao,
        Integer ordem // ordem opcional enviada pelo front; pode ser null
) {
}
