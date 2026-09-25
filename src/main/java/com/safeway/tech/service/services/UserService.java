package com.safeway.tech.service.services;

import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.UsuarioNotFoundException;
import com.safeway.tech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> listarUsuarios() {
        return userRepository.findAll();
    }

    public User buscarPorId(UUID idUsuario) {
        return userRepository.getReferenceById(idUsuario);
    }

    public User salvarUsuario(User user) {
        return userRepository.save(user);
    }

    public User alterarUsuario(User novoUser, UUID idUsuario) {
        User user = userRepository.findById(idUsuario).orElseThrow(RuntimeException::new);
        user.setNome(novoUser.getNome());
        user.setEmail(novoUser.getEmail());
        user.setTel1(novoUser.getTel1());
        user.setTel2(novoUser.getTel2());
        return userRepository.save(user);
    }

    public void excluir(UUID idUsuario) {
        if (!userRepository.existsById(idUsuario)) {
            throw new UsuarioNotFoundException("Usuário com ID " + idUsuario + " não encontrado.");
        }

        userRepository.deleteById(idUsuario);
    }
}
