package ru.mfa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mfa.dto.AuthDtos.TokenResponse;
import ru.mfa.model.SessionStatus;
import ru.mfa.model.UserSession;
import ru.mfa.repository.UserSessionRepository;
import ru.mfa.security.JwtTokenProvider;

import java.time.Instant;

/**
 * WHAT DOES THIS SERVICE DO?
 *
 * It manages the lifecycle of token pairs (access + refresh).
 *
 * 1. createSession()  → called at login, generates tokens and saves session to DB
 * 2. refresh()        → called at /auth/refresh, validates refresh token,
 *                       marks old session as USED, creates new session
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionRepository sessionRepository;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    // -----------------------------------------------------------------------
    // Called at login: generate tokens and save session
    // -----------------------------------------------------------------------
    @Transactional
    public TokenResponse createSession(String email, String role, String deviceId) {
        String accessToken  = jwtTokenProvider.generateAccessToken(email, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email, role);

        Instant now = Instant.now();
        UserSession session = UserSession.builder()
                .userEmail(email)
                .deviceId(deviceId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiry(now.plusMillis(accessTokenExpirationMs))
                .refreshTokenExpiry(now.plusMillis(refreshTokenExpirationMs))
                .status(SessionStatus.ACTIVE)
                .build();

        sessionRepository.save(session);

        return new TokenResponse(accessToken, refreshToken);
    }

    // -----------------------------------------------------------------------
    // Called at /auth/refresh: validate refresh token, rotate tokens
    // -----------------------------------------------------------------------
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        // Step 1: validate the JWT signature and expiry
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }

        // Step 2: make sure it's actually a refresh token (not an access token)
        String type = jwtTokenProvider.getTypeFromToken(refreshToken);
        if (!"refresh".equals(type)) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }

        // Step 3: find the session in the database
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        // Step 4: check session status
        if (session.getStatus() == SessionStatus.USED) {
            // Someone is trying to reuse a token! Mark as REVOKED (possible attack)
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(session);
            throw new IllegalArgumentException("Refresh token already used — possible replay attack");
        }

        if (session.getStatus() == SessionStatus.REVOKED) {
            throw new IllegalArgumentException("Session has been revoked");
        }

        // Step 5: check expiry from DB (double-check)
        if (session.getRefreshTokenExpiry().isBefore(Instant.now())) {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(session);
            throw new IllegalArgumentException("Refresh token expired");
        }

        // Step 6: mark old session as USED (prevent reuse)
        session.setStatus(SessionStatus.USED);
        sessionRepository.save(session);

        // Step 7: create a new session with new tokens
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String role  = jwtTokenProvider.getRoleFromToken(refreshToken);

        return createSession(email, role, session.getDeviceId());
    }
}
