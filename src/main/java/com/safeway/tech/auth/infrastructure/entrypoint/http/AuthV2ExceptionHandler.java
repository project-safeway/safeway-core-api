package com.safeway.tech.auth.infrastructure.entrypoint.http;

import com.safeway.tech.auth.core.exception.EmailAlreadyInUseException;
import com.safeway.tech.auth.core.exception.InvalidCredentialsException;
import com.safeway.tech.auth.core.exception.InvalidPasswordResetCodeException;
import com.safeway.tech.auth.core.exception.PasswordResetDeliveryException;
import com.safeway.tech.auth.core.exception.TransportAlreadyInUseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthV2Controller.class)
public class AuthV2ExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidPasswordResetCodeException.class)
    public ResponseEntity<String> handleInvalidPasswordResetCode(InvalidPasswordResetCodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PasswordResetDeliveryException.class)
    public ResponseEntity<String> handlePasswordResetDelivery(PasswordResetDeliveryException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<String> handleEmailAlreadyInUse(EmailAlreadyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(TransportAlreadyInUseException.class)
    public ResponseEntity<String> handleTransportAlreadyInUse(TransportAlreadyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
