package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.aluno.AlunoResponse;
import com.safeway.tech.api.dto.transporte.TransporteResponse;
import com.safeway.tech.domain.models.Transport;

import java.util.List;

public class TransporteMapper {

    public static TransporteResponse toResponse(Transport transport) {
        List<AlunoResponse> alunos = null;
        if (transport.getAlunosTransportes() != null) {
            alunos = transport.getAlunosTransportes().stream()
                    .map(AlunoMapper::toResponse)
                    .toList();
        }

        return new TransporteResponse(
                transport.getId(),
                transport.getPlaca(),
                transport.getModelo(),
                transport.getCapacidade(),
                alunos
        );
    }

}
