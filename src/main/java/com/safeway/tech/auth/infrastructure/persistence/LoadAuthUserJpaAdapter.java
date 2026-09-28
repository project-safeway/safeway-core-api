package com.safeway.tech.auth.infrastructure.persistence;

import com.safeway.tech.auth.core.model.AuthUser;
import com.safeway.tech.auth.core.port.LoadAuthUserPort;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoadAuthUserJpaAdapter implements LoadAuthUserPort {

    private final UserRepository userRepository;

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toAuthUser);
    }

    private AuthUser toAuthUser(User user) {
        return new AuthUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getTransport().getId()
        );
    }
}
