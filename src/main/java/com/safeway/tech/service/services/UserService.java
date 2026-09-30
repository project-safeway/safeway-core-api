package com.safeway.tech.service.services;

import com.safeway.tech.domain.models.User;
import com.safeway.tech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(UUID userId) {
        return userRepository.getReferenceById(userId);
    }
}
