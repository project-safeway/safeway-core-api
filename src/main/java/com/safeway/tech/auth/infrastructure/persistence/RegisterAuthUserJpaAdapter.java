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
    public boolean existsByPlaca(String placa) {
        return transportRepository.findByPlaca(placa).isPresent();
    }

    @Override
    @Transactional
    public RegisteredAuthUser create(RegisterAuthUserData data) {
        User user = new User();
        user.setNome(data.nome());
        user.setEmail(data.email());
        user.setPasswordHash(data.passwordHash());
        user.setRole(UserRole.COMMON);
        user.setTel1(data.telefone());
        User savedUser = userRepository.save(user);

        Transport transport = new Transport();
        transport.setPlaca(data.transportePlaca());
        transport.setModelo(data.transporteModelo());
        transport.setCapacidade(data.transporteCapacidade());
        transport.setUser(savedUser);
        Transport savedTransport = transportRepository.save(transport);

        return new RegisteredAuthUser(savedUser.getId(), savedTransport.getId());
    }
}

