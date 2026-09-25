package com.safeway.tech.infra.exception;

public class GuardianNotFoundException extends RuntimeException {
    public GuardianNotFoundException(String message) {
        super(message);
    }
}
