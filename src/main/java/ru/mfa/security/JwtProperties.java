package ru.mfa.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Reads the "jwt:" section from application.yml automatically.
 *
 * jwt:
 *   secret: mysecretkey
 *   access-token-expiration-ms: 900000
 *   refresh-token-expiration-ms: 604800000
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long accessTokenExpirationMs;
    private long refreshTokenExpirationMs;
}
