package com.safeway.tech.auth.core.exception;

public class PasswordResetDeliveryException extends RuntimeException {

    public PasswordResetDeliveryException(String message) {
        super(message);
    }

    public PasswordResetDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
