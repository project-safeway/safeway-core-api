package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.route.google.RotasRequest;
import com.safeway.tech.api.dto.route.google.RotasResponse;
import com.safeway.tech.service.services.RotasCompostasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rotas")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class RotasController {

    private final RotasCompostasService rotasCompostasService;

    @PostMapping("/otimizar")
    public RotasResponse otimizarRota(@RequestBody RotasRequest request) {
        return rotasCompostasService.otimizarMelhorRota(request);
    }


}
