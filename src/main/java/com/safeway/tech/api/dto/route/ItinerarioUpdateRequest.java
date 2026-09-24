package com.safeway.tech.api.dto.route;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.safeway.tech.domain.enums.RouteTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Time;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItinerarioUpdateRequest(
        @NotBlank String nome,
        Time horarioInicio,
        Time horarioFim,
        @NotNull RouteTypeEnum tipoViagem,
        @NotNull Boolean ativo,
        List<@Valid ItinerarioAlunoRequest> alunos,
        List<ItinerarioParadaUpdate> paradas
) {
    public record ItinerarioParadaUpdate(
            String tipo,      // "ALUNO" ou "ESCOLA"
            UUID id,          // alunoId ou escolaId
            Integer ordemGlobal,
            Integer ordemEspecifica // ordemEmbarque ou ordemVisita
    ) {
    }
}
