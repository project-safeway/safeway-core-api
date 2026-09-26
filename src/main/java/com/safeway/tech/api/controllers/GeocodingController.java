package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.route.google.RotasRequest;
import com.safeway.tech.api.dto.route.google.RotasResponse;
import com.safeway.tech.service.services.RouteOptimizationService;
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
public class GeocodingController {

    private final RouteOptimizationService routeOptimizationService;

    @PostMapping("/otimizar")
    public RotasResponse otimizarRota(@RequestBody RotasRequest request) {
        return routeOptimizationService.optimizeBestRoute(request);
    }


}
