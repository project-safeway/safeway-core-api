package com.safeway.tech.auth.core.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email ou password inválidos");
    }
}
