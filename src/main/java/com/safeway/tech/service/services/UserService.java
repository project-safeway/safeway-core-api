package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.user.UserProfileRequest;
import com.safeway.tech.auth.core.exception.EmailAlreadyInUseException;
import com.safeway.tech.auth.core.exception.TransportAlreadyInUseException;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.TransportRepository;
import com.safeway.tech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TransportRepository transportRepository;
    private final CurrentUserService currentUserService;

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User was not found"));
    }

    public User findCurrentUser() {
        return findById(currentUserService.getCurrentUserId());
    }

    @Transactional
    public User updateCurrentUser(UserProfileRequest request) {
        User user = findCurrentUser();

        if (userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
            throw new EmailAlreadyInUseException();
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user.setPrimaryPhoneNumber(request.primaryPhoneNumber());
        user.setSecondaryPhoneNumber(request.secondaryPhoneNumber());

        if (request.transport() != null) {
            Transport transport = user.getTransport() != null ? user.getTransport() : new Transport();
            boolean licensePlateInUse = transportRepository.findByLicensePlate(request.transport().licensePlate())
                    .filter(existing -> !existing.getId().equals(transport.getId()))
                    .isPresent();
            if (licensePlateInUse) {
                throw new TransportAlreadyInUseException();
            }

            transport.setLicensePlate(request.transport().licensePlate());
            transport.setModel(request.transport().model());
            transport.setCapacity(request.transport().capacity());
            user.setTransport(transportRepository.save(transport));
        }

        return userRepository.save(user);
    }
}
