package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.itinerario.ItinerarioAlunoResponse;
import com.safeway.tech.api.dto.itinerario.ItinerarioEscolaResponse;
import com.safeway.tech.api.dto.itinerario.ItinerarioResponse;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteStudent;
import com.safeway.tech.domain.models.ItinerarioEscola;

public class ItinerarioMapper {

    public static ItinerarioResponse toResponse(Route route) {
        return new ItinerarioResponse(
                route.getId(),
                route.getNome(),
                route.getHorarioInicio(),
                route.getHorarioFim(),
                route.getTipoViagem(),
                route.getAtivo(),
                route.getAlunos().stream().map(ItinerarioMapper::toAlunoResponse).toList(),
                route.getEscolas().stream().map(ItinerarioMapper::toEscolaResponse).toList()
        );
    }

    public static ItinerarioAlunoResponse toAlunoResponse(RouteStudent routeStudent) {
        return new ItinerarioAlunoResponse(
                routeStudent.getStudent().getId(),
                routeStudent.getStudent().getNome(),
                routeStudent.getOrdemEmbarque(),
                routeStudent.getAddress().getId(),
                routeStudent.getOrdemGlobal(),
                routeStudent.getStudent().getSchool().getNome(),
                routeStudent.getStudent().getResponsaveis().getFirst().getNome(),
                routeStudent.getStudent().getSala()
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
