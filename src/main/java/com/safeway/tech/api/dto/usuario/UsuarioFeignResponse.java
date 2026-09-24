package com.safeway.tech.api.dto.usuario;

import com.safeway.tech.domain.models.User;

import java.util.UUID;

public record UsuarioFeignResponse(
        UUID id,
        String nome,
        String email,
        Boolean ativo
) {

    public static UsuarioFeignResponse fromEntity(User entity) {
        return new UsuarioFeignResponse(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getAtivo()
        );
    }
}
