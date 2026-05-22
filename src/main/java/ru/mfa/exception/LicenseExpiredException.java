package ru.mfa.exception;

import java.time.LocalDateTime;

public class LicenseExpiredException extends RuntimeException {

    private final LocalDateTime expiresAt;

    public LicenseExpiredException(LocalDateTime expiresAt) {
        super("License expired");
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
