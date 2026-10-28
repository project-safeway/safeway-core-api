package com.safeway.tech.auth.infrastructure.entrypoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestV2(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        String email,
        @NotBlank(message = "Password is required")
        String password
) {
}
