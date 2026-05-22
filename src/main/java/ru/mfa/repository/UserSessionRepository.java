package ru.mfa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mfa.model.UserSession;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    // Find a session by its refresh token value
    // Used when the client sends a refresh request
    Optional<UserSession> findByRefreshToken(String refreshToken);

    // Find a session by its access token value
    // Used when the client sends a logout request
    Optional<UserSession> findByAccessToken(String accessToken);
}
