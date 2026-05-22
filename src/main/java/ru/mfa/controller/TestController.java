package ru.mfa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple test endpoints to verify your authentication works.
 *
 * Test with Postman or curl:
 *
 *   GET /api/hello
 *   Header: Authorization: Bearer <your_access_token>
 *   → Should return: "Hello, alice@example.com!"
 *
 *   GET /api/admin/secret
 *   Header: Authorization: Bearer <admin_access_token>
 *   → Should return admin message
 *   → Returns 403 if you use a USER token
 */
@RestController
@RequestMapping("/api")
public class TestController {

    // Any authenticated user (USER or ADMIN)
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
            "message", "Hello, " + authentication.getName() + "!",
            "role", authentication.getAuthorities().iterator().next().getAuthority()
        ));
    }

    // ADMIN only — will return 403 for regular users
    @GetMapping("/admin/secret")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminOnly() {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome, Admin! This is a protected endpoint."
        ));
    }
}
