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
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private UserResponse toResponse(User u) {
        return UserResponse.fromEntity(u);
    }

    @PostMapping
    public UserResponse createUser(@RequestBody User user) {
        User salvo = userService.saveUser(user);
        return toResponse(salvo);
    }

    @GetMapping("/{userId}")
    public UserResponse findById(@PathVariable UUID userId) {
        return toResponse(userService.findById(userId));
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable UUID userId) {
        userService.deleteUser(userId);
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(@RequestBody User novoUser, @PathVariable UUID userId) {
        User atualizado = userService.updateUser(novoUser, userId);
        return toResponse(atualizado);
    }

    /*

        MÉTODOS USADOS NO FEIGN CLIENT

     */

    @GetMapping("/feign/{userId}")
    public UserFeignResponse getUser(@PathVariable UUID userId) {
        User user = userService.findById(userId);
        return UserFeignResponse.fromEntity(user);
    }
}
