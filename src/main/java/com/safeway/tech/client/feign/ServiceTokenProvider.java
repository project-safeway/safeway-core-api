package com.safeway.tech.client.feign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth.service.base-url:http://localhost:8080}")
    private String authServiceBaseUrl;

    @Value("${auth.service.token-endpoint:/auth/v2/token/service}")
    private String authServiceTokenEndpoint;

    @Value("${auth.service.client-id:safeway-core}")
    private String authServiceClientId;

    @Value("${auth.service.client-secret:change-me}")
    private String authServiceClientSecret;

    private String cachedToken;
    private Instant tokenExpiration;

    public synchronized String getServiceToken() {

        if (cachedToken != null && tokenExpiration != null && Instant.now().isBefore(tokenExpiration)) {
            log.debug("Usando token de serviço em cache, expira em: {}", tokenExpiration);
            return cachedToken;
        }

        log.info("Solicitando novo token de serviço ao Auth V2");

        WebClient client = webClientBuilder.baseUrl(authServiceBaseUrl).build();
        ServiceTokenResponse response = client.post()
                .uri(authServiceTokenEndpoint)
                .bodyValue(new ServiceTokenRequest(authServiceClientId, authServiceClientSecret))
                .retrieve()
                .bodyToMono(ServiceTokenResponse.class)
                .block();

        if (response == null || response.accessToken() == null || response.expiresIn() == null) {
            throw new IllegalStateException("Resposta inválida ao solicitar token de serviço no Auth V2");
        }

        cachedToken = response.accessToken();
        tokenExpiration = Instant.now().plusSeconds(response.expiresIn()).minusSeconds(60);

        log.info("Token de serviço gerado, expira em: {}", tokenExpiration);
        return cachedToken;
    }

    private record ServiceTokenRequest(String clientId, String clientSecret) {
    }

    private record ServiceTokenResponse(String accessToken, Long expiresIn) {
    }

}
