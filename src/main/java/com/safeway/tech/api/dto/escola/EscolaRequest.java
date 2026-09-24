package com.safeway.tech.api.dto.escola;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.safeway.tech.api.dto.endereco.EnderecoRequest;
import com.safeway.tech.domain.enums.EducationLevelEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EscolaRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotNull EducationLevelEnum nivelEnsino,
        @NotNull @Valid EnderecoRequest endereco
) {
}
