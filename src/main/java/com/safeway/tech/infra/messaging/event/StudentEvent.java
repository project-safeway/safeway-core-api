package com.safeway.tech.infra.messaging.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudentEvent(
        UUID id,
        UUID alunoId,
        UUID usuarioId,
        String nome,
        Double valorMensalidade,
        Integer diaVencimento,
        Boolean ativo,
        String tipo,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
}
