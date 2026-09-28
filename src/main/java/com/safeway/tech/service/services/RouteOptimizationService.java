package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.google.RouteRequest;
import com.safeway.tech.api.dto.route.google.RouteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class RouteOptimizationService {

    private final List<IOptimizerService> providers;

    public RouteOptimizationService(List<IOptimizerService> providers) {
        this.providers = providers;
        if (providers != null && !providers.isEmpty()) {
            log.info("RotasCompostasService inicializado com {} provider(es)", providers.size());
            providers.forEach(p -> System.out.println("  - " + p.providerName()));
        } else {
            log.warn("⚠️ AVISO: Nenhum provider de rotas foi encontrado!");
        }
    }

    public RouteResponse optimizeBestRoute(RouteRequest request) {
        if (providers == null || providers.isEmpty()) {
            throw new RuntimeException(
                    "Nenhum provider de rotas disponível. " +
                            "Verifique se AdaptadorOtimizacaoGoogle está configurado como @Service"
            );
        }

        return providers.stream()
                .map(provedor -> {
                    try {
                        log.info("Chamando provider: {}", provedor.providerName());
                        long startTime = System.currentTimeMillis();
                        RouteResponse response = provedor.optimizeRoute(request);
                        long endTime = System.currentTimeMillis();
                        log.info("Provedor {} respondeu em {}ms", provedor.providerName(), endTime - startTime);
                        return response;
                    } catch (Exception e) {
                        log.error("Provedor {} falhou: {}", provedor.providerName(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(RouteResponse::totalDistance))
                .orElseThrow(() -> new RuntimeException("Nenhuma rota disponível de nenhum provider"));
    }
}
