package ru.mfa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mfa.model.AppUser;
import ru.mfa.model.SessionStatus;
import ru.mfa.model.UserSession;
import ru.mfa.repository.UserRepository;
import ru.mfa.repository.UserSessionRepository;
import ru.mfa.security.JwtService;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TokenPairService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshExpirationMs;

    public TokenPairService(JwtService jwtService,
                            UserRepository userRepository,
                            UserSessionRepository sessionRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    // Called at POST /auth/login
    @Transactional
    public Map<String, String> login(String username, String password) {
        // This is called AFTER password is already verified by AuthController
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return createTokenPair(user);
    }

    // Called at POST /auth/refresh
    @Transactional
    public Map<String, String> refresh(String refreshToken) {
        // Step 1: validate JWT signature + expiry
        if (!jwtService.isValid(refreshToken)) {
            throw new RuntimeException("Refresh token is invalid or expired");
        }
        if (!"refresh".equals(jwtService.getType(refreshToken))) {
            throw new RuntimeException("Not a refresh token");
        }

        // Step 2: find the session in DB
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Step 3: detect replay attack — token already used
        if (session.getStatus() == SessionStatus.REFRESHED) {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(session);
            throw new RuntimeException("Refresh token already used — possible replay attack");
        }

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Session is not active");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new RuntimeException("Refresh token expired");
        }

        // Step 4: mark old session as used
        session.setStatus(SessionStatus.REFRESHED);
        sessionRepository.save(session);

        // Step 5: issue new token pair
        AppUser user = session.getUser();
        return createTokenPair(user);
    }

    // Creates tokens + saves new session to DB
    private Map<String, String> createTokenPair(AppUser user) {
        String accessToken  = jwtService.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getRole());

        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession(
                user,
                refreshToken,
                now,
                now.plusSeconds(refreshExpirationMs / 1000)
        );
        sessionRepository.save(session);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }
}
