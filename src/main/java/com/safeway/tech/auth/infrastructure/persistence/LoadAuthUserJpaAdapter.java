package com.safeway.tech.auth.infrastructure.persistence;

import com.safeway.tech.auth.core.model.AuthUser;
import com.safeway.tech.auth.core.port.LoadAuthUserPort;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadAuthUserJpaAdapter implements LoadAuthUserPort {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(this::toAuthUser);
    }

    private AuthUser toAuthUser(User user) {
        return new AuthUser(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getTransporte().getId()
        );
    }
}
