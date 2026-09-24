package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.google.RotasRequest;
import com.safeway.tech.api.dto.route.google.RotasResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class RotasCompostasService {

    private List<RotasService> provedores;

    public RotasCompostasService(List<RotasService> provedores) {
        this.provedores = provedores;
        if (provedores != null && !provedores.isEmpty()) {
            System.out.println("RotasCompostasService inicializado com " + provedores.size() + " provedor(es)");
            provedores.forEach(p -> System.out.println("  - " + p.nomeProvedor()));
        } else {
            System.err.println("⚠️ AVISO: Nenhum provedor de rotas foi encontrado!");
        }
    }

    public RotasResponse otimizarMelhorRota(RotasRequest request) {
        if (provedores == null || provedores.isEmpty()) {
            throw new RuntimeException(
                    "Nenhum provedor de rotas disponível. " +
                            "Verifique se AdaptadorOtimizacaoGoogle está configurado como @Service"
            );
        }

        return provedores.stream()
                .map(provedor -> {
                    try {
                        System.out.println("Chamando provedor: " + provedor.nomeProvedor());
                        long startTime = System.currentTimeMillis();
                        RotasResponse response = provedor.otimizarRota(request);
                        long endTime = System.currentTimeMillis();
                        System.out.println("Provedor " + provedor.nomeProvedor() + " respondeu em " + (endTime - startTime) + "ms");
                        return response;
                    } catch (Exception e) {
                        System.err.println("Provedor " + provedor.nomeProvedor() + " falhou: " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(RotasResponse::distanciaTotal))
                .orElseThrow(() -> new RuntimeException("Nenhuma rota disponível de nenhum provedor"));
    }

    public RotasResponse otimizarComProvedor(String nomeProvedor, RotasRequest request) {
        if (provedores == null || provedores.isEmpty()) {
            throw new RuntimeException("Nenhum provedor de rotas disponível");
        }

        return provedores.stream()
                .filter(p -> p.nomeProvedor().equalsIgnoreCase(nomeProvedor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Provedor não encontrado: " + nomeProvedor))
                .otimizarRota(request);
    }
}
