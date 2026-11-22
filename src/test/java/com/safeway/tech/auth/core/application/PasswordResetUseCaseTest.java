package com.safeway.tech.auth.core.application;

import com.safeway.tech.auth.core.exception.InvalidPasswordResetCodeException;
import com.safeway.tech.auth.core.model.AuthUser;
import com.safeway.tech.auth.core.port.LoadAuthUserPort;
import com.safeway.tech.auth.core.port.PasswordHasherPort;
import com.safeway.tech.auth.core.port.PasswordResetNotifierPort;
import com.safeway.tech.auth.core.port.UpdateUserPasswordPort;
import com.safeway.tech.auth.infrastructure.persistence.InMemoryPasswordResetTokenAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetUseCaseTest {

    @Mock
    private LoadAuthUserPort loadAuthUserPort;

    @Mock
    private PasswordResetNotifierPort notifierPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private UpdateUserPasswordPort updateUserPasswordPort;

    private InMemoryPasswordResetTokenAdapter tokenAdapter;
    private Instant now;
    private PasswordResetUseCase useCase;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        tokenAdapter = new InMemoryPasswordResetTokenAdapter();
        now = Instant.parse("2026-11-22T20:00:00Z");
        user = new AuthUser(UUID.randomUUID(), "Motorista", "motorista@safeway.com", "oldHash", "ADMIN", UUID.randomUUID());
        useCase = new PasswordResetUseCase(
                loadAuthUserPort,
                tokenAdapter,
                notifierPort,
                passwordHasherPort,
                updateUserPasswordPort,
                new SecureRandom(),
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void requestAndResetPasswordShouldUpdateHashAndInvalidateCodeAfterUse() {
        when(loadAuthUserPort.findByEmail("motorista@safeway.com")).thenReturn(Optional.of(user));
        when(passwordHasherPort.hash("NovaSenha@1")).thenReturn("hashedNew");

        useCase.requestReset("  Motorista@Safeway.com ");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(notifierPort).notifyCode(anyString(), codeCaptor.capture(), anyLong());
        String code = codeCaptor.getValue();
        assertTrue(code.matches("\\d{6}"));

        useCase.resetPassword("motorista@safeway.com", code, "NovaSenha@1");
        verify(updateUserPasswordPort).updatePasswordHash(user.id(), "hashedNew");

        assertThrows(
                InvalidPasswordResetCodeException.class,
                () -> useCase.resetPassword("motorista@safeway.com", code, "OutraSenha@2")
        );
    }

    @Test
    void requestResetShouldIgnoreUnknownEmailWithoutNotifying() {
        when(loadAuthUserPort.findByEmail("naoexiste@safeway.com")).thenReturn(Optional.empty());

        useCase.requestReset("naoexiste@safeway.com");

        verify(notifierPort, never()).notifyCode(anyString(), anyString(), anyLong());
    }

    @Test
    void resetPasswordShouldRejectExpiredCode() {
        when(loadAuthUserPort.findByEmail("motorista@safeway.com")).thenReturn(Optional.of(user));
        useCase.requestReset("motorista@safeway.com");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(notifierPort).notifyCode(anyString(), codeCaptor.capture(), anyLong());

        PasswordResetUseCase afterExpiry = new PasswordResetUseCase(
                loadAuthUserPort,
                tokenAdapter,
                notifierPort,
                passwordHasherPort,
                updateUserPasswordPort,
                new SecureRandom(),
                Clock.fixed(now.plus(Duration.ofMinutes(16)), ZoneOffset.UTC)
        );

        assertThrows(
                InvalidPasswordResetCodeException.class,
                () -> afterExpiry.resetPassword("motorista@safeway.com", codeCaptor.getValue(), "NovaSenha@1")
        );
        verify(updateUserPasswordPort, never()).updatePasswordHash(any(), anyString());
    }

    @Test
    void resetPasswordShouldInvalidateCodeAfterMaxInvalidAttempts() {
        when(loadAuthUserPort.findByEmail("motorista@safeway.com")).thenReturn(Optional.of(user));
        useCase.requestReset("motorista@safeway.com");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(notifierPort).notifyCode(anyString(), codeCaptor.capture(), anyLong());
        String realCode = codeCaptor.getValue();
        String wrongCode = realCode.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 5; i++) {
            assertThrows(
                    InvalidPasswordResetCodeException.class,
                    () -> useCase.resetPassword("motorista@safeway.com", wrongCode, "NovaSenha@1")
            );
        }

        InvalidPasswordResetCodeException ex = assertThrows(
                InvalidPasswordResetCodeException.class,
                () -> useCase.resetPassword("motorista@safeway.com", realCode, "NovaSenha@1")
        );
        assertEquals("Código de recuperação inválido ou expirado.", ex.getMessage());
    }
}
