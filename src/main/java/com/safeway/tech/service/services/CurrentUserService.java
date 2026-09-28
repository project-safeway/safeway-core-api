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
            throw new IllegalStateException("Usuário não autenticado");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            return UUID.fromString(sub);
        }
        // Fallback: em alguns providers, getName() é o subject
        String name = auth.getName();
        return UUID.fromString(name);
    }

    public UUID getCurrentTransporteId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt jwt) {
            Object transporteClaim = jwt.getClaim("transport");

            if (transporteClaim == null) {
                throw new IllegalStateException("Claim 'transport' não encontrada no token");
            }

            return UUID.fromString(transporteClaim.toString());
        }

        throw new IllegalStateException("Token JWT não encontrado no authentication principal");
    }
}

