package com.safeway.tech.api.dto.usuario;

import com.safeway.tech.api.dto.transporte.TransporteResponse;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.service.mappers.TransporteMapper;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        String tel1,
        String tel2,
        TransporteResponse transporte,
        String role
) {
    public static UsuarioResponse fromEntity(User u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getTel1(),
                u.getTel2(),
                u.getTransporte() != null ? TransporteMapper.toResponse(u.getTransporte()) : null,
                u.getRole() != null ? u.getRole().name() : null
        );
    }
}

