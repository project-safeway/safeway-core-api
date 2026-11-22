package com.safeway.tech.auth.infrastructure.entrypoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestV2(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        String email,

        @NotBlank(message = "Code is required")
        @Pattern(regexp = "^\\d{6}$", message = "Code must have 6 digits")
        String code,

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must have at least 6 characters")
        String newPassword
) {
}
