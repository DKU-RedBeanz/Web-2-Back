package com.redbeanz.backend.auth;

import com.redbeanz.backend.user.UserRole;
import java.time.Duration;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JwtConfig {
    static final String ISSUER = "redbeanz-backend";

    @Bean
    SecretKey jwtSigningKey(@Value("${JWT_SECRET_BASE64:}") String configuredKey) {
        byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(configuredKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWT_SECRET_BASE64 must be valid Base64");
        }
        if (decodedKey.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET_BASE64 must encode at least 32 bytes");
        }
        return new SecretKeySpec(decodedKey, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSigningKey).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
        JwtTimestampValidator timestamp = new JwtTimestampValidator(Duration.ZERO);
        timestamp.setAllowEmptyExpiryClaim(false);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            timestamp,
            new JwtIssuerValidator(ISSUER),
            new JwtClaimValidator<>(JwtClaimNames.SUB, JwtConfig::isUserId),
            new JwtClaimValidator<Object>("role", role ->
                UserRole.USER.name().equals(role) || UserRole.ADMIN.name().equals(role))
        ));
        return decoder;
    }

    private static boolean isUserId(Object subject) {
        if (!(subject instanceof String userId)) {
            return false;
        }
        try {
            return Long.parseLong(userId) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
