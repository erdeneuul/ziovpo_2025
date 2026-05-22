package ru.mfa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mfa.dto.AuthDtos.*;
import ru.mfa.model.Role;
import ru.mfa.model.User;
import ru.mfa.repository.UserRepository;

/**
 * WHAT DOES THIS SERVICE DO?
 *
 * register() → creates a new user in the database
 *              - validates email is not taken
 *              - hashes the password with BCrypt
 *              - saves to DB
 *              - returns tokens
 *
 * login()    → checks credentials and returns tokens
 *              - finds user by email
 *              - compares provided password with stored hash
 *              - returns tokens if match
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public TokenResponse register(RegisterRequest request, String deviceId) {
        // Check email is not already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Create user with hashed password
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER) // new users are always USER by default
                .build();

        userRepository.save(user);

        // Generate and return tokens
        return tokenService.createSession(
                user.getEmail(),
                user.getRole().name(),
                deviceId
        );
    }

    public TokenResponse login(LoginRequest request, String deviceId) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // Compare plain password with BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Generate and return tokens
        return tokenService.createSession(
                user.getEmail(),
                user.getRole().name(),
                deviceId
        );
    }
}
