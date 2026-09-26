package com.safeway.tech.auth.core.application;

public record RegisterTransportCommand(
        String licensePlate,
        String model,
        Integer capacity
) {
}
