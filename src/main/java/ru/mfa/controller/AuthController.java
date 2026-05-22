package ru.mfa.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.mfa.dto.AuthDtos.*;
import ru.mfa.model.SessionStatus;
import ru.mfa.model.UserSession;
import ru.mfa.repository.LicenseRepository;
import ru.mfa.repository.UserSessionRepository;
import ru.mfa.service.AuthService;
import ru.mfa.service.TokenService;

import java.util.Map;
import java.util.Optional;

/**
 * WHAT IS A CONTROLLER?
 * It's where HTTP requests arrive and responses leave.
 *
 * ENDPOINTS:
 *
 *   POST /auth/register
 *     Body: { "email": "...", "password": "..." }
 *     Returns: { "accessToken": "...", "refreshToken": "..." }
 *
 *   POST /auth/login
 *     Body: { "email": "...", "password": "..." }
 *     Returns: { "accessToken": "...", "refreshToken": "..." }
 *
 *   POST /auth/refresh
 *     Body: { "refreshToken": "..." }
 *     Returns: { "accessToken": "...", "refreshToken": "..." }  (new pair!)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final UserSessionRepository sessionRepository;
    private final LicenseRepository licenseRepository;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String deviceId = httpRequest.getHeader("X-Device-Id");
        if (deviceId == null) deviceId = "unknown";

        TokenResponse tokens = authService.register(request, deviceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String deviceId = httpRequest.getHeader("X-Device-Id");
        if (deviceId == null) deviceId = "unknown";

        TokenResponse tokens = authService.login(request, deviceId);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {

        TokenResponse tokens = tokenService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }

    // GET /auth/me — returns the currently authenticated user's email and role
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(Map.of(
                "email", authentication.getName(),
                "role", role
        ));
    }

    // POST /auth/logout — revokes the current session and clears the user's license association
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Optional<UserSession> session = sessionRepository.findByAccessToken(token);
            session.ifPresent(s -> {
                s.setStatus(SessionStatus.REVOKED);
                sessionRepository.save(s);

                // Clear license association so GET /api/license/status returns 404 after logout
                licenseRepository.findByUserEmail(s.getUserEmail()).ifPresent(lic -> {
                    lic.setUser(null);
                    licenseRepository.save(lic);
                });
            });
        }
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
