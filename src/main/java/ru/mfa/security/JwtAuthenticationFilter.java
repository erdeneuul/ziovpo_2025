package ru.mfa.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * WHAT IS A FILTER?
 * Think of it as a checkpoint at the entrance of a building.
 * Every HTTP request passes through this filter BEFORE reaching any controller.
 *
 * HOW IT WORKS:
 * 1. Client sends request with header:  Authorization: Bearer xxxxx.yyyyy.zzzzz
 * 2. This filter extracts the token from the header
 * 3. Validates the token (checks signature + expiry)
 * 4. If valid: tells Spring "this user is authenticated" → request proceeds
 * 5. If invalid or missing: request continues as anonymous → Spring Security
 *    will block it if the endpoint requires authentication
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String type = jwtTokenProvider.getTypeFromToken(token);

            // Only access tokens are valid for regular API calls
            // Refresh tokens are only valid for /auth/refresh endpoint
            if ("access".equals(type)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                // Tell Spring Security: "this user is logged in with this role"
                var authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Always continue to the next filter/controller
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT from the Authorization header.
     * Expected format: "Bearer <token>"
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // remove "Bearer " prefix
        }
        return null;
    }
}
