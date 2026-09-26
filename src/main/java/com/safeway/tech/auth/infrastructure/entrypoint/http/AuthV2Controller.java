package com.safeway.tech.auth.infrastructure.entrypoint.http;

import com.safeway.tech.auth.core.application.LoginCommand;
import com.safeway.tech.auth.core.application.LoginResult;
import com.safeway.tech.auth.core.application.LoginUseCase;
import com.safeway.tech.auth.core.application.RegisterTransportCommand;
import com.safeway.tech.auth.core.application.RegisterUserCommand;
import com.safeway.tech.auth.core.application.RegisterUserUseCase;
import com.safeway.tech.auth.infrastructure.entrypoint.dto.AuthResponseV2;
import com.safeway.tech.auth.infrastructure.entrypoint.dto.LoginRequestV2;
import com.safeway.tech.auth.infrastructure.entrypoint.dto.RegisterRequestV2;
import com.safeway.tech.auth.infrastructure.entrypoint.dto.ServiceTokenRequestV2;
import com.safeway.tech.auth.infrastructure.entrypoint.dto.ServiceTokenResponseV2;
import com.safeway.tech.auth.infrastructure.security.ServiceJwtTokenIssuer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/auth/v2", "/auth"})
@RequiredArgsConstructor
public class AuthV2Controller {

    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final ServiceJwtTokenIssuer serviceJwtTokenIssuer;

    @Value("${auth.v2.service-token.client-id:safeway-core}")
    private String serviceClientId;

    @Value("${auth.v2.service-token.client-secret:change-me}")
    private String serviceClientSecret;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestV2 request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.name(),
                request.email(),
                request.password(),
                request.phoneNumber(),
                new RegisterTransportCommand(
                        request.transport().licensePlate(),
                        request.transport().model(),
                        request.transport().capacity()
                )
        );

        registerUserUseCase.execute(command);
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseV2> login(@Valid @RequestBody LoginRequestV2 request) {
        LoginResult result = loginUseCase.execute(new LoginCommand(request.email(), request.password()));

        AuthResponseV2 response = new AuthResponseV2(
                result.accessToken(),
                result.expiresIn(),
                result.username(),
                result.userId(),
                result.transportId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/service")
    public ResponseEntity<ServiceTokenResponseV2> issueServiceToken(@Valid @RequestBody ServiceTokenRequestV2 request) {
        if (!serviceClientId.equals(request.clientId()) || !serviceClientSecret.equals(request.clientSecret())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais de serviço inválidas");
        }

        var issuedToken = serviceJwtTokenIssuer.issueFor(request.clientId());
        return ResponseEntity.ok(new ServiceTokenResponseV2(issuedToken.value(), issuedToken.expiresIn()));
    }
}
