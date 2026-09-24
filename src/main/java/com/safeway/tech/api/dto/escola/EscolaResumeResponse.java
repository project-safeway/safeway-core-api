package com.safeway.tech.api.dto.escola;

import com.safeway.tech.api.dto.endereco.EnderecoResponse;
import com.safeway.tech.domain.enums.EducationLevelEnum;

public record EscolaResumeResponse(
        String nome,
        EducationLevelEnum nivelEnsino,
        EnderecoResponse endereco
) {
}
