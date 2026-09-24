package com.safeway.tech.api.dto.escola;

import com.safeway.tech.api.dto.aluno.AlunoResumeResponse;
import com.safeway.tech.api.dto.endereco.EnderecoResponse;
import com.safeway.tech.domain.enums.EducationLevelEnum;

import java.util.List;
import java.util.UUID;

public record EscolaResponse(
        UUID id,
        String nome,
        EducationLevelEnum nivelEnsino,
        EnderecoResponse endereco,
        List<AlunoResumeResponse> alunos
) {
}
