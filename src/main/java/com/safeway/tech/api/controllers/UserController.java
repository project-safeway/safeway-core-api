package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.user.UserFeignResponse;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.service.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /*

        MÉTODOS USADOS NO FEIGN CLIENT

     */

    @GetMapping("/feign/{userId}")
    public UserFeignResponse getUser(@PathVariable UUID userId) {
        User user = userService.findById(userId);
        return UserFeignResponse.fromEntity(user);
    }
}
