package com.safeway.tech.auth.core.application;

import com.safeway.tech.auth.core.exception.EmailAlreadyInUseException;
import com.safeway.tech.auth.core.exception.TransportAlreadyInUseException;
import com.safeway.tech.auth.core.model.RegisterAuthUserData;
import com.safeway.tech.auth.core.model.RegisteredAuthUser;
import com.safeway.tech.auth.core.port.PasswordHasherPort;
import com.safeway.tech.auth.core.port.RegisterAuthUserPort;

public class RegisterUserUseCase {

    private final RegisterAuthUserPort registerAuthUserPort;
    private final PasswordHasherPort passwordHasherPort;

    public RegisterUserUseCase(
            RegisterAuthUserPort registerAuthUserPort,
            PasswordHasherPort passwordHasherPort
    ) {
        this.registerAuthUserPort = registerAuthUserPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    public RegisterUserResult execute(RegisterUserCommand command) {
        if (registerAuthUserPort.existsByEmail(command.email())) {
            throw new EmailAlreadyInUseException();
        }

        String normalizedLicensePlate = command.transport().licensePlate().trim().toUpperCase();
        if (registerAuthUserPort.existsByLicensePlate(normalizedLicensePlate)) {
            throw new TransportAlreadyInUseException();
        }

        RegisterAuthUserData data = new RegisterAuthUserData(
                command.name(),
                command.email(),
                passwordHasherPort.hash(command.password()),
                command.phoneNumber(),
                normalizedLicensePlate,
                command.transport().model(),
                command.transport().capacity()
        );

        RegisteredAuthUser registeredAuthUser = registerAuthUserPort.create(data);

        return new RegisterUserResult(registeredAuthUser.userId(), registeredAuthUser.transportId());
    }
}
