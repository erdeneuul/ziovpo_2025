package ru.mfa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.mfa.dto.AuthDtos.ActivationRequest;
import ru.mfa.dto.AuthDtos.LicenseTicketDto;
import ru.mfa.exception.*;
import ru.mfa.model.User;
import ru.mfa.repository.UserRepository;
import ru.mfa.service.LicenseService;

import java.util.Map;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    private final UserRepository userRepository;

    @GetMapping("/status")
    public ResponseEntity<Object> getStatus(Authentication authentication) {
        try {
            User user = resolveUser(authentication);
            LicenseTicketDto ticket = licenseService.getStatus(user);
            return ResponseEntity.ok(ticket);
        } catch (LicenseNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "No license found"));
        } catch (LicenseExpiredException e) {
            return ResponseEntity.status(402).body(Map.of(
                    "error", "License expired",
                    "expiresAt", e.getExpiresAt().toString()
            ));
        } catch (LicenseBlockedException e) {
            return ResponseEntity.status(403).body(Map.of("error", "License is blocked"));
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<Object> activate(
            @Valid @RequestBody ActivationRequest request,
            Authentication authentication) {
        try {
            User user = resolveUser(authentication);
            LicenseTicketDto ticket = licenseService.activate(request.getActivationCode(), user);
            return ResponseEntity.ok(ticket);
        } catch (LicenseNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Activation code not found"));
        } catch (LicenseAlreadyUsedException e) {
            return ResponseEntity.status(409).body(Map.of("error", "Activation code already used"));
        } catch (LicenseExpiredException e) {
            return ResponseEntity.status(402).body(Map.of("error", "License expired"));
        } catch (LicenseBlockedException e) {
            return ResponseEntity.status(403).body(Map.of("error", "License is blocked"));
        }
    }

    private User resolveUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
