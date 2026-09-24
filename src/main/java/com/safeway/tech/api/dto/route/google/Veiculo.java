package com.safeway.tech.api.dto.route.google;

public record Veiculo(
        String id,
        Localizacao localizacaoInicial,
        Localizacao localizacaoFinal
) {
}
