package com.safeway.tech.auth.infrastructure.entrypoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestV2(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @Valid
        @NotNull(message = "Transport details are required")
        TransportRequestV2 transport
) {
}
