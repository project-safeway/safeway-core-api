package com.safeway.tech.infra.messaging.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudentEvent(
        @JsonProperty("id") UUID id,
        @JsonProperty("alunoId") UUID studentId,
        @JsonProperty("usuarioId") UUID userId,
        @JsonProperty("nome") String name,
        @JsonProperty("valorMensalidade") Double monthlyFee,
        @JsonProperty("diaVencimento") Integer dueDate,
        @JsonProperty("ativo") Boolean active,
        @JsonProperty("tipo") String type,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("timestamp") LocalDateTime timestamp
) {
}
