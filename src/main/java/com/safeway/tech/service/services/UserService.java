package com.safeway.tech.service.services;

import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.UserNotFoundException;
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

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User newUserData, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        user.setName(newUserData.getName());
        user.setEmail(newUserData.getEmail());
        user.setPrimaryPhoneNumber(newUserData.getPrimaryPhoneNumber());
        user.setSecondaryPhoneNumber(newUserData.getSecondaryPhoneNumber());
        return userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuário com ID " + userId + " não encontrado.");
        }

        userRepository.deleteById(userId);
    }
}
