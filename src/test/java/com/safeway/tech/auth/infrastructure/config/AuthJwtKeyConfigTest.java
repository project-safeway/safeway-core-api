package com.safeway.tech.auth.infrastructure.config;

import com.safeway.tech.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthJwtKeyConfigTest {

    @TempDir
    Path directory;

    @Test
    void issuedTokenIsAcceptedByResourceServer() throws Exception {
        KeyPair pair = generateKeyPair();
        Path publicKey = writePem("pub.key", "PUBLIC KEY", pair.getPublic().getEncoded());
        Path privateKey = writePem("pri.key", "PRIVATE KEY", pair.getPrivate().getEncoded());
        AuthJwtKeyConfig config = configure(publicKey.toString(), privateKey.toString());
        SecurityConfig securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "publicKeyLocation", publicKey.toString());
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .subject("test-user")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();

        String token = config.authV2JwtEncoder(config.authV2RsaKeyPair())
                .encode(JwtEncoderParameters.from(claims)).getTokenValue();

        assertEquals("test-user", securityConfig.jwtDecoder().decode(token).getSubject());
    }

    @Test
    void rejectsMismatchedKeys() throws Exception {
        Path publicKey = writePem("pub.key", "PUBLIC KEY", generateKeyPair().getPublic().getEncoded());
        Path privateKey = writePem("pri.key", "PRIVATE KEY", generateKeyPair().getPrivate().getEncoded());
        AuthJwtKeyConfig config = configure(publicKey.toString(), privateKey.toString());

        assertThrows(IllegalStateException.class, config::authV2RsaKeyPair);
    }

    @Test
    void rejectsMissingPrivateKeyConfiguration() {
        AuthJwtKeyConfig config = configure("classpath:keys/pub.key", "");

        assertThrows(IllegalStateException.class, config::authV2RsaKeyPair);
    }

    @Test
    void rejectsUnreadablePrivateKeyInsteadOfGeneratingTemporaryKeys() throws Exception {
        Path publicKey = writePem("pub.key", "PUBLIC KEY", generateKeyPair().getPublic().getEncoded());
        AuthJwtKeyConfig config = configure(publicKey.toString(), directory.resolve("missing.key").toString());

        assertThrows(IllegalStateException.class, config::authV2RsaKeyPair);
    }

    @Test
    void rejectsMalformedPrivateKeyInsteadOfGeneratingTemporaryKeys() throws Exception {
        Path publicKey = writePem("pub.key", "PUBLIC KEY", generateKeyPair().getPublic().getEncoded());
        Path privateKey = Files.writeString(directory.resolve("pri.key"), "invalid-key");
        AuthJwtKeyConfig config = configure(publicKey.toString(), privateKey.toString());

        assertThrows(IllegalStateException.class, config::authV2RsaKeyPair);
    }

    private AuthJwtKeyConfig configure(String publicKey, String privateKey) {
        AuthJwtKeyConfig config = new AuthJwtKeyConfig();
        ReflectionTestUtils.setField(config, "publicKeyLocation", publicKey);
        ReflectionTestUtils.setField(config, "privateKeyLocation", privateKey);
        return config;
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private Path writePem(String name, String type, byte[] encoded) throws Exception {
        String content = "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
        return Files.writeString(directory.resolve(name), content);
    }
}
