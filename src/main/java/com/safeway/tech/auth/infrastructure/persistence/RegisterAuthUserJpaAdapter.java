package com.safeway.tech.auth.infrastructure.persistence;

import com.safeway.tech.auth.core.model.RegisterAuthUserData;
import com.safeway.tech.auth.core.model.RegisteredAuthUser;
import com.safeway.tech.auth.core.port.RegisterAuthUserPort;
import com.safeway.tech.domain.enums.UserRole;
import com.safeway.tech.domain.models.Transporte;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.repository.TransporteRepository;
import com.safeway.tech.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterAuthUserJpaAdapter implements RegisterAuthUserPort {

    private final UsuarioRepository usuarioRepository;
    private final TransporteRepository transporteRepository;

    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPlaca(String placa) {
        return transporteRepository.findByPlaca(placa).isPresent();
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
        User savedUser = usuarioRepository.save(user);

        Transporte transporte = new Transporte();
        transporte.setPlaca(data.transportePlaca());
        transporte.setModelo(data.transporteModelo());
        transporte.setCapacidade(data.transporteCapacidade());
        transporte.setUser(savedUser);
        Transporte savedTransporte = transporteRepository.save(transporte);

        return new RegisteredAuthUser(savedUser.getId(), savedTransporte.getId());
    }
}

