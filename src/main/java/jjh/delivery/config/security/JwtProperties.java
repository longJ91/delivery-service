package jjh.delivery.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpiration,
        long refreshTokenExpiration
) {
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            secret = "default-secret-key-for-development-only-change-in-production";
        }
        if (accessTokenExpiration <= 0) {
            accessTokenExpiration = 3600000; // 1 hour
        }
        if (refreshTokenExpiration <= 0) {
            refreshTokenExpiration = 604800000; // 7 days
        }
    }
}
