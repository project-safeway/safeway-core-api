package com.safeway.tech.auth.core.exception;

public class TransportAlreadyInUseException extends RuntimeException {

    public TransportAlreadyInUseException() {
        super("License plate is already registered");
    }
}
