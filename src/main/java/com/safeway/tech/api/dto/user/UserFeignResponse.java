package com.safeway.tech.api.dto.user;

import java.util.UUID;

public record UserFeignResponse(
        UUID id,
        String name,
        String email,
        Boolean active
) {
}
