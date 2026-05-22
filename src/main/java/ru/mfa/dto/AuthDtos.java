package ru.mfa.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

// ============================================================
// REQUEST: what the client sends when registering
// ============================================================
// POST /auth/register
// Body: { "email": "alice@example.com", "password": "SecurePass1!" }

public class AuthDtos {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
            regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*",
            message = "Password must contain at least one special character"
        )
        private String password;
    }

    // ============================================================
    // REQUEST: what the client sends when logging in
    // ============================================================
    @Data
    public static class LoginRequest {
        @NotBlank private String email;
        @NotBlank private String password;
    }

    // ============================================================
    // REQUEST: what the client sends when refreshing tokens
    // ============================================================
    @Data
    public static class RefreshRequest {
        @NotBlank private String refreshToken;
    }

    // ============================================================
    // RESPONSE: what the server returns after login or refresh
    // ============================================================
    // { "accessToken": "xxx.yyy.zzz", "refreshToken": "aaa.bbb.ccc" }
    @Data
    public static class TokenResponse {
        private final String accessToken;
        private final String refreshToken;
    }
}
