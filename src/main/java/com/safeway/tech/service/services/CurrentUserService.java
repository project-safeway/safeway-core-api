package com.safeway.tech.service.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserService {

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            return UUID.fromString(sub);
        }
        // Some providers use getName() as the subject.
        String name = auth.getName();
        return UUID.fromString(name);
    }

    public UUID getCurrentTransportId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt jwt) {
            Object transportClaim = jwt.getClaim("transport");

            if (transportClaim == null) {
                throw new IllegalStateException("Claim 'transport' was not found in the token");
            }

            return UUID.fromString(transportClaim.toString());
        }

        throw new IllegalStateException("JWT token was not found in the authentication principal");
    }
}
