package ru.mfa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.mfa.security.JwtAuthenticationFilter;

/**
 * WHAT IS THIS?
 * This is the "security rulebook" for our application.
 * It defines:
 *   - Which endpoints are public (no login needed)
 *   - Which endpoints require authentication
 *   - Which endpoints require ADMIN role
 *   - That we use JWT instead of sessions (STATELESS)
 *
 * ROLE-BASED ACCESS CONTROL (RBAC):
 *   Everyone can: POST /auth/login, POST /auth/register, POST /auth/refresh
 *   Logged-in users can: GET /api/hello
 *   Only admins can: anything under /api/admin/**
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // enables @PreAuthorize on methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(csrf -> csrf.disable())

            // Use STATELESS sessions — no server-side session storage
            // Every request must carry its own JWT token
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Define access rules
            .authorizeHttpRequests(auth -> auth

                // Public endpoints — no token required
                .requestMatchers("/auth/login",
                                 "/auth/register",
                                 "/auth/refresh").permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // License endpoints require authentication
                .requestMatchers("/api/license/**").authenticated()

                // All other endpoints require any authenticated user
                .anyRequest().authenticated()
            )

            // Add our JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCryptPasswordEncoder hashes passwords.
     * NEVER store plain passwords in the database!
     *
     * BCrypt turns "mypassword123" into something like:
     *   "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
