package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.user.UserFeignResponse;
import com.safeway.tech.api.dto.user.UserResponse;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private UserResponse toResponse(User u) {
        return UserResponse.fromEntity(u);
    }

    @PostMapping
    public UserResponse salvarUsuario(@RequestBody User user) {
        User salvo = userService.saveUser(user);
        return toResponse(salvo);
    }

    @GetMapping("/{idUsuario}")
    public UserResponse retornarUm(@PathVariable UUID idUsuario) {
        return toResponse(userService.findById(idUsuario));
    }

    @DeleteMapping("/{idUsuario}")
    public void excluir(@PathVariable UUID idUsuario) {
        userService.deleteUser(idUsuario);
    }

    @PutMapping("/{idUsuario}")
    public UserResponse alterarUsuario(@RequestBody User novoUser, @PathVariable UUID idUsuario) {
        User atualizado = userService.updateUser(novoUser, idUsuario);
        return toResponse(atualizado);
    }

    /*

        MÉTODOS USADOS NO FEIGN CLIENT

     */

    @GetMapping("/feign/{idUsuario}")
    public UserFeignResponse buscarUsuario(@PathVariable UUID idUsuario) {
        User user = userService.findById(idUsuario);
        return UserFeignResponse.fromEntity(user);
    }
}
