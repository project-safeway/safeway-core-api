package com.safeway.tech.auth.infrastructure.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class AuthJwtKeyConfig {

    @Value("${jwt.public.key:}")
    private String publicKeyLocation;

    @Value("${jwt.private.key:}")
    private String privateKeyLocation;

    @Bean("authV2RsaKeyPair")
    public KeyPair authV2RsaKeyPair() {
        if (publicKeyLocation == null || publicKeyLocation.isBlank()
                || privateKeyLocation == null || privateKeyLocation.isBlank()) {
            throw new IllegalStateException("jwt.public.key and jwt.private.key must be configured");
        }

        try (InputStream publicKeyStream = loadInputStream(publicKeyLocation);
                InputStream privateKeyStream = loadInputStream(privateKeyLocation)) {
            RSAPublicKey publicKey = RsaKeyConverters.x509().convert(publicKeyStream);
            RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(privateKeyStream);
            if (publicKey == null || privateKey == null
                    || !publicKey.getModulus().equals(privateKey.getModulus())) {
                throw new IllegalStateException("jwt.public.key and jwt.private.key must be a matching RSA key pair");
            }
            return new KeyPair(publicKey, privateKey);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Failed to load JWT RSA keys; configure jwt.public.key (X.509) and jwt.private.key (PKCS#8)", e);
        }
    }

    @Bean("authV2JwtEncoder")
    public JwtEncoder authV2JwtEncoder(@Qualifier("authV2RsaKeyPair") KeyPair keyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    private InputStream loadInputStream(String location) throws IOException {
        if (location.startsWith("classpath:")) {
            String path = location.substring("classpath:".length());
            return new ClassPathResource(path).getInputStream();
        }
        return new FileInputStream(location);
    }
}
