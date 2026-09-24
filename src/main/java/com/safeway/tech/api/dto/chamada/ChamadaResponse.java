package com.safeway.tech.api.dto.chamada;

import com.safeway.tech.api.dto.itinerario.ItinerarioResponse;
import com.safeway.tech.domain.enums.AttendanceStatusEnum;

import java.util.List;
import java.util.UUID;

public record ChamadaResponse(
        UUID id,
        ItinerarioResponse itinerario,
        AttendanceStatusEnum status,
        List<ChamadaAlunoResponse> alunos
) {
}
