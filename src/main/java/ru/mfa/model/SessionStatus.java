package ru.mfa.model;

public enum SessionStatus {
    ACTIVE,   // refresh token can be used
    USED,     // refresh token was already used once
    REVOKED   // refresh token was revoked (suspicious activity or logout)
}
