package com.safeway.tech.auth.core.model;

public record RegisterAuthUserData(
        String name,
        String email,
        String passwordHash,
        String phoneNumber,
        String transportLicensePlate,
        String transportModel,
        Integer transportCapacity
) {
}
