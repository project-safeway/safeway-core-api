package com.safeway.tech.api.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record UserFeignResponse(
        UUID id,
        @JsonProperty("nome") String name,
        String email,
        @JsonProperty("ativo") Boolean active
) {
}
