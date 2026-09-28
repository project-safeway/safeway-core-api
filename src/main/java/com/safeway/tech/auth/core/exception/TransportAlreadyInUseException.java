package com.safeway.tech.auth.core.exception;

public class TransportAlreadyInUseException extends RuntimeException {

    public TransportAlreadyInUseException() {
        super("Placa já cadastrada");
    }
}
