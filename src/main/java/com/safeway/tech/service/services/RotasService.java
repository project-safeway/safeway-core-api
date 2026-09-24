package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.google.RotasRequest;
import com.safeway.tech.api.dto.route.google.RotasResponse;

public interface RotasService {
    RotasResponse otimizarRota(RotasRequest request);
    String nomeProvedor();
}
