package com.safeway.tech.client.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinancialServiceAuthInterceptor implements RequestInterceptor {

    private final ServiceTokenProvider serviceTokenProvider;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = resolveToken();
        requestTemplate.header("Authorization", "Bearer " + token);
    }

    private String resolveToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            log.debug("Forwarding the user token to the financial service");
            return jwt.getTokenValue();
        }

        log.debug("No authenticated user; obtaining a service token for the financial service");
        return serviceTokenProvider.getServiceToken();
    }
}
