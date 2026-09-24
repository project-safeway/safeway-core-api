package com.safeway.tech.api.dto.user;

import com.safeway.tech.domain.models.User;

import java.util.UUID;

public record UserFeignResponse(
        UUID id,
        String name,
        String email,
        Boolean active
) {

    public static UserFeignResponse fromEntity(User entity) {
        return new UserFeignResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.isActive()
        );
    }
}
