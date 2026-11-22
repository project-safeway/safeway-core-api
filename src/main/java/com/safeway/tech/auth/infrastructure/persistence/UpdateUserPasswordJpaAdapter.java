package com.safeway.tech.auth.infrastructure.persistence;

import com.safeway.tech.auth.core.port.UpdateUserPasswordPort;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateUserPasswordJpaAdapter implements UpdateUserPasswordPort {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void updatePasswordHash(UUID userId, String passwordHash) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User was not found"));
        user.setPasswordHash(passwordHash);
        userRepository.save(user);
    }
}
