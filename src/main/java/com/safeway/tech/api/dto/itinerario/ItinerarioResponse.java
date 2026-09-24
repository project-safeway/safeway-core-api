package com.safeway.tech.api.dto.itinerario;

import com.safeway.tech.domain.enums.RouteTypeEnum;

import java.sql.Time;
import java.util.List;
import java.util.UUID;

public record ItinerarioResponse(
        UUID id,
        String nome,
        Time horarioInicio,
        Time horarioFim,
        RouteTypeEnum tipoViagem,
        Boolean ativo,
        List<ItinerarioAlunoResponse> alunos,
        List<ItinerarioEscolaResponse> escolas
) {
}
