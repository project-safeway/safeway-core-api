package com.safeway.tech.auth.core.application;

public record LoginCommand(
        String email,
        String password
) {
}

