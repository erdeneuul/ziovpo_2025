package ru.mfa.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * WHAT IS A SESSION?
 * When a user logs in, we create a "session" record in the database.
 * This session stores the refresh token so we can:
 *   1. Check if a refresh token is valid
 *   2. Mark it as USED after it's been used once
 *   3. Detect if someone tries to use it again (replay attack)
 *
 * SESSION STATUSES:
 *   ACTIVE  → refresh token is valid and has never been used
 *   USED    → refresh token was used once to get a new pair of tokens
 *   REVOKED → something suspicious happened, session is blocked
 */
@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userEmail;         // which user owns this session

    private String deviceId;          // which device/client made the request

    @Column(length = 512)
    private String accessToken;       // the access token issued

    @Column(length = 512)
    private String refreshToken;      // the refresh token issued (used to find this record)

    private Instant accessTokenExpiry;   // when the access token expires
    private Instant refreshTokenExpiry;  // when the refresh token expires

    @Enumerated(EnumType.STRING)
    private SessionStatus status;     // ACTIVE, USED, or REVOKED
}
