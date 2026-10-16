package com.safeway.tech.auth.infrastructure.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class AuthJwtKeyConfig {

    private static final Logger log = LoggerFactory.getLogger(AuthJwtKeyConfig.class);

    @Value("${jwt.public.key:}")
    private String publicKeyLocation;

    @Value("${jwt.private.key:}")
    private String privateKeyLocation;

    @Bean("authV2RsaKeyPair")
    public KeyPair authV2RsaKeyPair() {
        try {
            if (publicKeyLocation != null && !publicKeyLocation.isBlank()
                    && privateKeyLocation != null && !privateKeyLocation.isBlank()) {
                RSAPublicKey publicKey = RsaKeyConverters.x509().convert(loadInputStream(publicKeyLocation));
                RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(loadInputStream(privateKeyLocation));
                return new KeyPair(publicKey, privateKey);
            }
        } catch (Exception e) {
            log.warn("Failed to load Auth V2 RSA keys ({} / {}). Generating in-memory keys. Details: {}",
                    publicKeyLocation, privateKeyLocation, e.getMessage());
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate Auth V2 RSA key pair", e);
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
