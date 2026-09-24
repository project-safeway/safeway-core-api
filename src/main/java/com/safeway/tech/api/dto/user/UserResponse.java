package com.safeway.tech.api.dto.user;

import com.safeway.tech.api.dto.transport.TransportResponse;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.service.mappers.TransportMapper;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String primaryPhoneNumber,
        String secondaryPhoneNumber,
        TransportResponse transport,
        String role
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPrimaryPhoneNumber(),
                user.getSecondaryPhoneNumber(),
                user.getTransport() != null ? TransportMapper.toResponse(user.getTransport()) : null,
                user.getRole() != null ? user.getRole().name() : null
        );
    }
}

