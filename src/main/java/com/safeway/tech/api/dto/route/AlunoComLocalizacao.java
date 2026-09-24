package com.safeway.tech.api.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record AlunoComLocalizacao(
        @JsonProperty("alunoId") UUID idAluno,
        @JsonProperty("nomeAluno") String nome,
        @JsonProperty("enderecoId") UUID idEndereco,
        String enderecoCompleto,
        Double latitude,
        Double longitude,
        @JsonProperty("ordemEmbarque") Integer ordem
) {
}
