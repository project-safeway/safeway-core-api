package com.safeway.tech.auth.infrastructure.config;

import com.safeway.tech.auth.core.application.LoginUseCase;
import com.safeway.tech.auth.core.application.PasswordResetUseCase;
import com.safeway.tech.auth.core.application.RegisterUserUseCase;
import com.safeway.tech.auth.core.port.LoadAuthUserPort;
import com.safeway.tech.auth.core.port.PasswordHasherPort;
import com.safeway.tech.auth.core.port.PasswordResetNotifierPort;
import com.safeway.tech.auth.core.port.PasswordResetTokenPort;
import com.safeway.tech.auth.core.port.PasswordVerifierPort;
import com.safeway.tech.auth.core.port.RegisterAuthUserPort;
import com.safeway.tech.auth.core.port.TokenIssuerPort;
import com.safeway.tech.auth.core.port.UpdateUserPasswordPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthCoreConfig {

    @Bean
    public LoginUseCase loginUseCase(
            LoadAuthUserPort loadAuthUserPort,
            PasswordVerifierPort passwordVerifierPort,
            TokenIssuerPort tokenIssuerPort
    ) {
        return new LoginUseCase(loadAuthUserPort, passwordVerifierPort, tokenIssuerPort);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            RegisterAuthUserPort registerAuthUserPort,
            PasswordHasherPort passwordHasherPort
    ) {
        return new RegisterUserUseCase(registerAuthUserPort, passwordHasherPort);
    }

    @Bean
    public PasswordResetUseCase passwordResetUseCase(
            LoadAuthUserPort loadAuthUserPort,
            PasswordResetTokenPort tokenPort,
            PasswordResetNotifierPort notifierPort,
            PasswordHasherPort passwordHasherPort,
            UpdateUserPasswordPort updateUserPasswordPort
    ) {
        return new PasswordResetUseCase(
                loadAuthUserPort,
                tokenPort,
                notifierPort,
                passwordHasherPort,
                updateUserPasswordPort
        );
    }
}
