package com.safeway.tech.auth.core.application;

public record RegisterUserCommand(
        String name,
        String email,
        String password,
        String phoneNumber,
        RegisterTransportCommand transport
) {
}
