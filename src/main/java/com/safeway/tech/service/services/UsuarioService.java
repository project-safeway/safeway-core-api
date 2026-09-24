package com.safeway.tech.service.services;

import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.UsuarioNotFoundException;
import com.safeway.tech.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<User> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public User buscarPorId(UUID idUsuario) {
        return usuarioRepository.getReferenceById(idUsuario);
    }

    public User salvarUsuario(User user) {
        return usuarioRepository.save(user);
    }

    public User alterarUsuario(User novoUser, UUID idUsuario) {
        User user = usuarioRepository.findById(idUsuario).orElseThrow(RuntimeException::new);
        user.setNome(novoUser.getNome());
        user.setEmail(novoUser.getEmail());
        user.setTel1(novoUser.getTel1());
        user.setTel2(novoUser.getTel2());
        return usuarioRepository.save(user);
    }

    public void excluir(UUID idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new UsuarioNotFoundException("Usuário com ID " + idUsuario + " não encontrado.");
        }

        usuarioRepository.deleteById(idUsuario);
    }
}
