package com.safeway.tech.api.dto.student;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record StudentFeignResponse(
        UUID id,
        @JsonProperty("nome") String name,
        @JsonProperty("valorMensalidade") Double monthlyFee,
        @JsonProperty("diaVencimento") Integer dueDate,
        @JsonProperty("ativo") Boolean active,
        UUID transportId,
        UUID motoristaId
) {
}
