package ru.mfa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.mfa.dto.TicketResponse;
import ru.mfa.model.AppUser;
import ru.mfa.model.License;
import ru.mfa.repository.UserRepository;
import ru.mfa.service.LicenseService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/license")
public class LicenseController {

    private final LicenseService licenseService;
    private final UserRepository userRepository;

    public LicenseController(LicenseService licenseService, UserRepository userRepository) {
        this.licenseService = licenseService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<License> createLicense(@RequestBody CreateLicenseRequest request) {
        License license = licenseService.createLicense(
                request.getProductId(),
                request.getTypeId(),
                request.getOwnerId(),
                request.getDeviceCount(),
                request.getDescription()
        );
        return ResponseEntity.ok(license);
    }

    @PostMapping("/activate")
    public ResponseEntity<TicketResponse> activateLicense(@RequestBody ActivateLicenseRequest request,
                                                          Authentication authentication) {
        UUID userId = resolveUserId(authentication);
        TicketResponse response = licenseService.activateLicense(
                request.getCode(),
                userId,
                request.getDeviceId(),
                request.getDeviceName(),
                request.getMacAddress()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<TicketResponse> checkLicense(@RequestParam String code,
                                                       @RequestParam UUID deviceId) {
        return ResponseEntity.ok(licenseService.checkLicense(code, deviceId));
    }

    @PostMapping("/renew/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<License> renewLicense(@PathVariable UUID id, Authentication authentication) {
        UUID userId = resolveUserId(authentication);
        return ResponseEntity.ok(licenseService.renewLicense(id, userId));
    }

    private UUID resolveUserId(Authentication authentication) {
        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // AppUser.id is Long; derive a deterministic UUID for license tracking
        return UUID.nameUUIDFromBytes(user.getId().toString().getBytes(StandardCharsets.UTF_8));
    }

    static class CreateLicenseRequest {
        private UUID productId;
        private UUID typeId;
        private UUID ownerId;
        private int deviceCount;
        private String description;

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public UUID getTypeId() { return typeId; }
        public void setTypeId(UUID typeId) { this.typeId = typeId; }

        public UUID getOwnerId() { return ownerId; }
        public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

        public int getDeviceCount() { return deviceCount; }
        public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    static class ActivateLicenseRequest {
        private String code;
        private UUID deviceId;
        private String deviceName;
        private String macAddress;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public UUID getDeviceId() { return deviceId; }
        public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public String getMacAddress() { return macAddress; }
        public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    }
}
