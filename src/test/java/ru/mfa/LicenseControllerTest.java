package ru.mfa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.mfa.model.*;
import ru.mfa.repository.LicenseRepository;
import ru.mfa.repository.UserRepository;
import ru.mfa.repository.UserSessionRepository;
import ru.mfa.security.JwtTokenProvider;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LicenseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired LicenseRepository licenseRepository;
    @Autowired UserSessionRepository sessionRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("Pass1!abc"))
                .role(Role.ROLE_USER)
                .build());

        accessToken = jwtTokenProvider.generateAccessToken(testUser.getEmail(), testUser.getRole().name());
    }

    // ------------------------------------------------------------------ helpers

    private License activeCode(String code) {
        return licenseRepository.save(License.builder()
                .activationCode(code)
                .status(LicenseStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusYears(1))
                .createdAt(LocalDateTime.now())
                .build());
    }

    private License expiredCode(String code) {
        return licenseRepository.save(License.builder()
                .activationCode(code)
                .status(LicenseStatus.EXPIRED)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now())
                .build());
    }

    private License blockedCode(String code) {
        return licenseRepository.save(License.builder()
                .activationCode(code)
                .status(LicenseStatus.BLOCKED)
                .expiresAt(LocalDateTime.now().plusYears(1))
                .createdAt(LocalDateTime.now())
                .build());
    }

    private String bearer() {
        return "Bearer " + accessToken;
    }

    private String activateBody(String code) throws Exception {
        return objectMapper.writeValueAsString(Map.of("activationCode", code));
    }

    // ------------------------------------------------------------------ /api/license/activate

    @Test
    void activate_validCode_returns200() throws Exception {
        activeCode("VALID-CODE-001");
        mockMvc.perform(post("/api/license/activate")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateBody("VALID-CODE-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void activate_invalidCode_returns404() throws Exception {
        mockMvc.perform(post("/api/license/activate")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateBody("NO-SUCH-CODE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Activation code not found"));
    }

    @Test
    void activate_alreadyUsedByOtherUser_returns409() throws Exception {
        User other = userRepository.save(User.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("Pass1!abc"))
                .role(Role.ROLE_USER)
                .build());

        License lic = activeCode("USED-CODE-001");
        lic.setUser(other);
        licenseRepository.save(lic);

        mockMvc.perform(post("/api/license/activate")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateBody("USED-CODE-001")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Activation code already used"));
    }

    @Test
    void activate_expiredCode_returns402() throws Exception {
        expiredCode("EXP-CODE-001");
        mockMvc.perform(post("/api/license/activate")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateBody("EXP-CODE-001")))
                .andExpect(status().is(402))
                .andExpect(jsonPath("$.error").value("License expired"));
    }

    @Test
    void activate_blockedCode_returns403() throws Exception {
        blockedCode("BLK-CODE-001");
        mockMvc.perform(post("/api/license/activate")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateBody("BLK-CODE-001")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("License is blocked"));
    }

    // ------------------------------------------------------------------ /api/license/status

    @Test
    void getStatus_noLicense_returns404() throws Exception {
        mockMvc.perform(get("/api/license/status")
                        .header("Authorization", bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No license found"));
    }

    @Test
    void getStatus_activeLicense_returns200() throws Exception {
        License lic = activeCode("STATUS-CODE-001");
        lic.setUser(testUser);
        licenseRepository.save(lic);

        mockMvc.perform(get("/api/license/status")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"));
    }

    @Test
    void getStatus_expiredLicense_returns402() throws Exception {
        License lic = expiredCode("STATUS-EXP-001");
        lic.setUser(testUser);
        licenseRepository.save(lic);

        mockMvc.perform(get("/api/license/status")
                        .header("Authorization", bearer()))
                .andExpect(status().is(402))
                .andExpect(jsonPath("$.error").value("License expired"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    // ------------------------------------------------------------------ /auth/me

    @Test
    void me_authenticated_returns200WithEmailAndRole() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    // ------------------------------------------------------------------ /auth/logout

    @Test
    void logout_revokeSession_returns200() throws Exception {
        // Create an active session with this access token
        UserSession session = UserSession.builder()
                .userEmail(testUser.getEmail())
                .deviceId("device-1")
                .accessToken(accessToken)
                .refreshToken("dummy-refresh-token")
                .accessTokenExpiry(Instant.now().plusSeconds(900))
                .refreshTokenExpiry(Instant.now().plusSeconds(604800))
                .status(SessionStatus.ACTIVE)
                .build();
        sessionRepository.save(session);

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));

        // Verify the session is now REVOKED
        UserSession revoked = sessionRepository.findByAccessToken(accessToken).orElseThrow();
        assert revoked.getStatus() == SessionStatus.REVOKED;
    }
}
