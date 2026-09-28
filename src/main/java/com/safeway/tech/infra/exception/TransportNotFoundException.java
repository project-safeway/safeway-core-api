package com.safeway.tech.infra.exception;

public class TransportNotFoundException extends RuntimeException {
    public TransportNotFoundException(String message) {
        super(message);
    }
}
