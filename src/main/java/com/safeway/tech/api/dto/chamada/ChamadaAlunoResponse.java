package com.safeway.tech.api.dto.chamada;

import com.safeway.tech.api.dto.aluno.AlunoResponse;
import com.safeway.tech.domain.enums.PresenceStatusEnum;

import java.time.LocalDateTime;

public record ChamadaAlunoResponse(
        AlunoResponse aluno,
        PresenceStatusEnum presenca,
        LocalDateTime dataHora
) {
}
