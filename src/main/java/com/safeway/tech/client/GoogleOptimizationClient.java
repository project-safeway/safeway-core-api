package com.safeway.tech.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auth.oauth2.GoogleCredentials;
import com.safeway.tech.api.dto.route.google.Localizacao;
import com.safeway.tech.api.dto.route.google.PontoParada;
import com.safeway.tech.api.dto.route.google.RotasRequest;
import com.safeway.tech.api.dto.route.google.Veiculo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Component
public class GoogleOptimizationClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String projectId;
    private GoogleCredentials credentials;

    public GoogleOptimizationClient(WebClient.Builder builder,
                                    @Value("${google.projectId:dev-local}") String configuredProjectId) {
        this.webClient = builder.baseUrl("https://routeoptimization.googleapis.com").build();
        this.projectId = resolveProjectId(configuredProjectId);
        try {
            this.credentials = initCredentials();
        } catch (IOException e) {
            System.err.println("Aviso: não foi possível inicializar credenciais Google. Detalhe: " + e.getMessage());
            this.credentials = null;
        }
    }

    private String resolveProjectId(String fallback) {
        String keyPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (keyPath != null && !keyPath.isBlank()) {
            try {
                String content = Files.readString(Path.of(keyPath));
                JsonNode node = mapper.readTree(content);
                String pid = node.path("project_id").asText();
                if (pid != null && !pid.isBlank()) {
                    System.out.println("projectId extraído da service account: " + pid);
                    return pid;
                }
                System.out.println("Service account sem project_id, usando configurado: " + fallback);
            } catch (Exception ex) {
                System.err.println("Falha ao ler project_id do arquivo: " + ex.getMessage());
            }
        } else {
            System.out.println("Sem GOOGLE_APPLICATION_CREDENTIALS, usando projectId configurado: " + fallback);
        }
        return fallback;
    }

    private GoogleCredentials initCredentials() throws IOException {
        String keyPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (keyPath != null && !keyPath.isBlank()) {
            try (FileInputStream in = new FileInputStream(keyPath)) {
                return GoogleCredentials.fromStream(in).createScoped("https://www.googleapis.com/auth/cloud-platform");
            }
        }
        return GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    public JsonNode otimizarRotas(RotasRequest request) {
        if (projectId == null || projectId.isBlank() || "dev-local".equals(projectId)) {
            throw new RuntimeException("projectId inválido. Configure 'google.projectId' ou defina GOOGLE_APPLICATION_CREDENTIALS com project_id.");
        }
        ObjectNode body = montarRequest(request);
        System.out.println("Google optimizeTours body: " + body);
        String uri = String.format("/v1/projects/%s:optimizeTours", projectId);
        try {
            return webClient.post()
                    .uri(uri)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(30));
        } catch (WebClientResponseException wcre) {
            throw new RuntimeException("Erro na API Google: " + wcre.getResponseBodyAsString(), wcre);
        } catch (Exception ex) {
            throw new RuntimeException("Falha inesperada ao chamar otimização Google: " + ex.getMessage(), ex);
        }
    }

    private ObjectNode montarRequest(RotasRequest request) {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode model = mapper.createObjectNode();

        ArrayNode shipments = mapper.createArrayNode();
        for (PontoParada p : request.pontosParada()) {
            ObjectNode shipment = mapper.createObjectNode();
            shipment.put("label", p.id());
            ArrayNode deliveries = mapper.createArrayNode();
            ObjectNode delivery = mapper.createObjectNode();
            delivery.set("arrivalLocation", criarLocalizacao(p.localizacao()));
            deliveries.add(delivery);
            shipment.set("deliveries", deliveries);
            shipments.add(shipment);
        }
        model.set("shipments", shipments);

        // Vehicles
        ArrayNode vehicles = mapper.createArrayNode();
        Veiculo v = request.veiculo();
        ObjectNode vehicle = mapper.createObjectNode();
        vehicle.put("label", v.id());
        vehicle.set("startLocation", criarLocalizacao(v.localizacaoInicial()));
        vehicle.set("endLocation", criarLocalizacao(v.localizacaoFinal()));
        vehicles.add(vehicle);
        model.set("vehicles", vehicles);

        root.set("model", model);
        return root;
    }

    private ObjectNode criarLocalizacao(Localizacao loc) {
        if (loc == null) {
            throw new RuntimeException("Localização ausente");
        }
        double lat = loc.lat();
        double lng = loc.lng();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new RuntimeException("Coordenadas inválidas: " + lat + ", " + lng);
        }
        // Ajuste: a API rejeitou campo latLng aninhado. Enviar diretamente latitude/longitude.
        ObjectNode location = mapper.createObjectNode();
        location.put("latitude", lat);
        location.put("longitude", lng);
        return location;
    }

    private String getAccessToken() {
        try {
            if (credentials != null) {
                credentials.refreshIfExpired();
                return credentials.getAccessToken().getTokenValue();
            }
            String env = System.getenv("GOOGLE_ACCESS_TOKEN");
            if (env != null && !env.isBlank()) {
                return env;
            }
            throw new RuntimeException("Sem credenciais Google. Defina GOOGLE_APPLICATION_CREDENTIALS ou GOOGLE_ACCESS_TOKEN.");
        } catch (IOException e) {
            String env = System.getenv("GOOGLE_ACCESS_TOKEN");
            if (env != null && !env.isBlank()) {
                return env;
            }
            throw new RuntimeException("Falha ao obter token Google: " + e.getMessage(), e);
        }
    }
}
