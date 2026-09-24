package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.chamada.ChamadaAlunoResponse;
import com.safeway.tech.api.dto.chamada.ChamadaResponse;
import com.safeway.tech.domain.models.Chamada;
import com.safeway.tech.domain.models.ChamadaAluno;

import java.util.ArrayList;
import java.util.List;

public class ChamadaMapper {

    public static ChamadaResponse toResponse(Chamada chamada) {
        List<ChamadaAlunoResponse> alunosResponse = new ArrayList<>();
        if (chamada.getAlunos() != null && !chamada.getAlunos().isEmpty()) {
            alunosResponse = chamada.getAlunos().stream()
                    .map(ChamadaMapper::toAlunoResponse)
                    .toList();
        }

        return new ChamadaResponse(
                chamada.getId(),
                ItinerarioMapper.toResponse(chamada.getItinerario()),
                chamada.getStatus(),
                alunosResponse
        );
    }

    public static ChamadaAlunoResponse toAlunoResponse(ChamadaAluno chamadaAluno) {
        return new ChamadaAlunoResponse(
                AlunoMapper.toResponse(chamadaAluno.getStudent()),
                chamadaAluno.getPresenca(),
                chamadaAluno.getData()
        );
    }

}
