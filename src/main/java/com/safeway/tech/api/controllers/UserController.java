package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.user.UserFeignResponse;
import com.safeway.tech.api.dto.user.UserProfileRequest;
import com.safeway.tech.api.dto.user.UserResponse;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.service.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(UserResponse.fromEntity(userService.findCurrentUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(UserResponse.fromEntity(userService.updateCurrentUser(request)));
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
