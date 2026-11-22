package com.safeway.tech.auth.core.exception;

public class InvalidPasswordResetCodeException extends RuntimeException {

    public InvalidPasswordResetCodeException() {
        super("Código de recuperação inválido ou expirado.");
    }
}
