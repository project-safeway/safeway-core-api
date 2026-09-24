package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.user.UserFeignResponse;
import com.safeway.tech.api.dto.user.UserResponse;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.service.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    private UserResponse toResponse(User u) {
        return UserResponse.fromEntity(u);
    }

    @PostMapping
    public UserResponse salvarUsuario(@RequestBody User user) {
        User salvo = usuarioService.salvarUsuario(user);
        return toResponse(salvo);
    }

    @GetMapping
    public List<UserResponse> listarUsuarios() {
        return usuarioService.listarUsuarios()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{idUsuario}")
    public UserResponse retornarUm(@PathVariable UUID idUsuario) {
        return toResponse(usuarioService.buscarPorId(idUsuario));
    }

    @DeleteMapping("/{idUsuario}")
    public void excluir(@PathVariable UUID idUsuario) {
        usuarioService.excluir(idUsuario);
    }

    @PutMapping("/{idUsuario}")
    public UserResponse alterarUsuario(@RequestBody User novoUser, @PathVariable UUID idUsuario) {
        User atualizado = usuarioService.alterarUsuario(novoUser, idUsuario);
        return toResponse(atualizado);
    }

    /*

        MÉTODOS USADOS NO FEIGN CLIENT

     */

    @GetMapping("/feign/{idUsuario}")
    public UserFeignResponse buscarUsuario(@PathVariable UUID idUsuario) {
        User user = usuarioService.buscarPorId(idUsuario);
        return UserFeignResponse.fromEntity(user);
    }
}
