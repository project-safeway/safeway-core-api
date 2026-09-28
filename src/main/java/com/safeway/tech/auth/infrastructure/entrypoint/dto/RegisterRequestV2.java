package com.safeway.tech.auth.infrastructure.entrypoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestV2(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String password,

        @NotBlank(message = "Telefone é obrigatório")
        String phoneNumber,

        @Valid
        @NotNull(message = "Dados do transport são obrigatórios")
        TransporteRequestV2 transport
) {
}

