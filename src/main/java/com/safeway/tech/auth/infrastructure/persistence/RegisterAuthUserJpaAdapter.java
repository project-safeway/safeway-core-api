package com.safeway.tech.auth.infrastructure.persistence;

import com.safeway.tech.auth.core.model.RegisterAuthUserData;
import com.safeway.tech.auth.core.model.RegisteredAuthUser;
import com.safeway.tech.auth.core.port.RegisterAuthUserPort;
import com.safeway.tech.domain.enums.UserRole;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.repository.TransportRepository;
import com.safeway.tech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterAuthUserJpaAdapter implements RegisterAuthUserPort {

    private final UserRepository userRepository;
    private final TransportRepository transportRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return transportRepository.findByLicensePlate(licensePlate).isPresent();
    }

    @Override
    @Transactional
    public RegisteredAuthUser create(RegisterAuthUserData data) {

        Transport transport = new Transport();
        transport.setLicensePlate(data.transportLicensePlate());
        transport.setModel(data.transportModel());
        transport.setCapacity(data.transportCapacity());
        Transport savedTransport = transportRepository.save(transport);

        User user = new User();
        user.setName(data.name());
        user.setEmail(data.email());
        user.setPasswordHash(data.passwordHash());
        user.setRole(UserRole.DRIVER);
        user.setPrimaryPhoneNumber(data.phoneNumber());
        user.setTransport(savedTransport);
        User savedUser = userRepository.save(user);

        return new RegisteredAuthUser(savedUser.getId(), savedTransport.getId());
    }
}

