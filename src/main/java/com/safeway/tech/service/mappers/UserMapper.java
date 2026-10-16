package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.user.UserFeignResponse;
import com.safeway.tech.api.dto.user.UserResponse;
import com.safeway.tech.domain.models.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
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

    public static UserFeignResponse toFeignResponse(User user) {
        return new UserFeignResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isActive()
        );
    }
}
