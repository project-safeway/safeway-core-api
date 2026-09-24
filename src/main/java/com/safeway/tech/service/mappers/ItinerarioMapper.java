package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.itinerario.ItinerarioAlunoResponse;
import com.safeway.tech.api.dto.itinerario.ItinerarioEscolaResponse;
import com.safeway.tech.api.dto.itinerario.ItinerarioResponse;
import com.safeway.tech.domain.models.Itinerario;
import com.safeway.tech.domain.models.ItinerarioAluno;
import com.safeway.tech.domain.models.ItinerarioEscola;

public class ItinerarioMapper {

    public static ItinerarioResponse toResponse(Itinerario itinerario) {
        return new ItinerarioResponse(
                itinerario.getId(),
                itinerario.getNome(),
                itinerario.getHorarioInicio(),
                itinerario.getHorarioFim(),
                itinerario.getTipoViagem(),
                itinerario.getAtivo(),
                itinerario.getAlunos().stream().map(ItinerarioMapper::toAlunoResponse).toList(),
                itinerario.getEscolas().stream().map(ItinerarioMapper::toEscolaResponse).toList()
        );
    }

    public static ItinerarioAlunoResponse toAlunoResponse(ItinerarioAluno itinerarioAluno) {
        return new ItinerarioAlunoResponse(
                itinerarioAluno.getStudent().getId(),
                itinerarioAluno.getStudent().getNome(),
                itinerarioAluno.getOrdemEmbarque(),
                itinerarioAluno.getAddress().getId(),
                itinerarioAluno.getOrdemGlobal(),
                itinerarioAluno.getStudent().getSchool().getNome(),
                itinerarioAluno.getStudent().getResponsaveis().getFirst().getNome(),
                itinerarioAluno.getStudent().getSala()
        );
    }

    public static ItinerarioEscolaResponse toEscolaResponse(ItinerarioEscola itinerarioEscola) {
        return new ItinerarioEscolaResponse(
                itinerarioEscola.getSchool().getId(),
                itinerarioEscola.getSchool().getNome(),
                itinerarioEscola.getAddress().getCidade(),
                itinerarioEscola.getOrdemParada(),
                itinerarioEscola.getAddress().getId(),
                itinerarioEscola.getOrdemGlobal()
        );
    }
}
