package ru.mfa.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * WHAT DOES THIS CLASS DO?
 *
 * It creates JWT tokens and reads/validates them.
 *
 * WHAT IS A JWT TOKEN?
 * A JWT looks like this:  xxxxx.yyyyy.zzzzz
 *
 *   xxxxx = Header  (algorithm used, e.g. HS256)
 *   yyyyy = Payload (data: email, role, expiry time)
 *   zzzzz = Signature (proves nobody tampered with it)
 *
 * Example payload inside a JWT:
 *   {
 *     "sub": "alice@example.com",
 *     "role": "ROLE_USER",
 *     "type": "access",
 *     "exp": 1700000000
 *   }
 *
 * The client sends this token in every request header:
 *   Authorization: Bearer xxxxx.yyyyy.zzzzz
 *
 * The server checks the signature — if it matches, the token is valid.
 * No database lookup needed for access tokens!
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // -----------------------------------------------------------------------
    // Generate tokens
    // -----------------------------------------------------------------------

    public String generateAccessToken(String email, String role) {
        return buildToken(email, role, "access", jwtProperties.getAccessTokenExpirationMs());
    }

    public String generateRefreshToken(String email, String role) {
        return buildToken(email, role, "refresh", jwtProperties.getRefreshTokenExpirationMs());
    }

    private String buildToken(String email, String role, String type, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)           // who the token belongs to
                .claim("role", role)      // what permissions they have
                .claim("type", type)      // "access" or "refresh"
                .issuedAt(now)            // when it was created
                .expiration(expiry)       // when it expires
                .signWith(getSigningKey()) // sign it so nobody can fake it
                .compact();               // produce the final string
    }

    // -----------------------------------------------------------------------
    // Read token data
    // -----------------------------------------------------------------------

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getTypeFromToken(String token) {
        return getClaims(token).get("type", String.class);
    }

    // -----------------------------------------------------------------------
    // Validate token
    // -----------------------------------------------------------------------

    public boolean validateToken(String token) {
        try {
            getClaims(token); // throws if invalid
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Helper: parse claims from token
    // -----------------------------------------------------------------------

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        // Convert the secret string to a cryptographic key
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
