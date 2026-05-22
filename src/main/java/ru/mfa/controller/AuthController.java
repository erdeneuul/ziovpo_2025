package ru.mfa.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mfa.dto.AuthDtos.*;
import ru.mfa.service.AuthService;
import ru.mfa.service.TokenService;

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
}
