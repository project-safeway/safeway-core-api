package com.safeway.tech.api.dto.route.google;

public record Veiculo(
        String id,
        Location locationInicial,
        Location locationFinal
) {
}
