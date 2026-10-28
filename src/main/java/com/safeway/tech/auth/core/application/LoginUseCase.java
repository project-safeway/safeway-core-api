package com.safeway.tech.auth.core.application;

import com.safeway.tech.auth.core.exception.InvalidCredentialsException;
import com.safeway.tech.auth.core.model.AuthUser;
import com.safeway.tech.auth.core.model.IssuedToken;
import com.safeway.tech.auth.core.port.LoadAuthUserPort;
import com.safeway.tech.auth.core.port.PasswordVerifierPort;
import com.safeway.tech.auth.core.port.TokenIssuerPort;

public class LoginUseCase {

    private final LoadAuthUserPort loadAuthUserPort;
    private final PasswordVerifierPort passwordVerifierPort;
    private final TokenIssuerPort tokenIssuerPort;

    public LoginUseCase(
            LoadAuthUserPort loadAuthUserPort,
            PasswordVerifierPort passwordVerifierPort,
            TokenIssuerPort tokenIssuerPort
    ) {
        this.loadAuthUserPort = loadAuthUserPort;
        this.passwordVerifierPort = passwordVerifierPort;
        this.tokenIssuerPort = tokenIssuerPort;
    }

    public LoginResult execute(LoginCommand command) {
        AuthUser authUser = loadAuthUserPort.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordVerifierPort.matches(command.password(), authUser.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        IssuedToken token = tokenIssuerPort.issueFor(authUser);

        return new LoginResult(
                token.value(),
                token.expiresIn(),
                authUser.name(),
                authUser.id(),
                authUser.transportId()
        );
    }
}

