package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.google.RotasRequest;
import com.safeway.tech.api.dto.route.google.RotasResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class RouteOptimizationService {

    private List<IOptimizerService> provedores;

    public RouteOptimizationService(List<IOptimizerService> provedores) {
        this.provedores = provedores;
        if (provedores != null && !provedores.isEmpty()) {
            log.info("RotasCompostasService inicializado com {} provedor(es)", provedores.size());
            provedores.forEach(p -> System.out.println("  - " + p.providerName()));
        } else {
            log.warn("⚠️ AVISO: Nenhum provedor de rotas foi encontrado!");
        }
    }

    public RotasResponse optimizeBestRoute(RotasRequest request) {
        if (provedores == null || provedores.isEmpty()) {
            throw new RuntimeException(
                    "Nenhum provedor de rotas disponível. " +
                            "Verifique se AdaptadorOtimizacaoGoogle está configurado como @Service"
            );
        }

        return provedores.stream()
                .map(provedor -> {
                    try {
                        log.info("Chamando provedor: {}", provedor.providerName());
                        long startTime = System.currentTimeMillis();
                        RotasResponse response = provedor.optimizeRoute(request);
                        long endTime = System.currentTimeMillis();
                        log.info("Provedor {} respondeu em {}ms", provedor.providerName(), endTime - startTime);
                        return response;
                    } catch (Exception e) {
                        log.error("Provedor {} falhou: {}", provedor.providerName(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(RotasResponse::distanciaTotal))
                .orElseThrow(() -> new RuntimeException("Nenhuma rota disponível de nenhum provedor"));
    }
}
