package ru.mfa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mfa.dto.Ticket;
import ru.mfa.dto.TicketResponse;
import ru.mfa.model.*;
import ru.mfa.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;

    public LicenseService(LicenseRepository licenseRepository,
                          LicenseTypeRepository licenseTypeRepository,
                          DeviceRepository deviceRepository,
                          DeviceLicenseRepository deviceLicenseRepository,
                          LicenseHistoryRepository licenseHistoryRepository) {
        this.licenseRepository = licenseRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.deviceRepository = deviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
    }

    @Transactional
    public License createLicense(UUID productId, UUID typeId, UUID ownerId,
                                 int deviceCount, String description) {
        LicenseType type = licenseTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("License type not found"));

        License license = new License();
        license.setCode(UUID.randomUUID().toString());
        license.setProductId(productId);
        license.setTypeId(typeId);
        license.setOwnerId(ownerId);
        license.setDeviceCount(deviceCount);
        license.setDescription(description);
        license.setBlocked(false);
        license.setFirstActivationDate(null);
        license.setEndingDate(LocalDate.now().plusDays(type.getDefaultDurationInDays()));

        License saved = licenseRepository.save(license);

        saveHistory(saved.getId(), ownerId, "CREATED");

        return saved;
    }

    @Transactional
    public TicketResponse activateLicense(String code, UUID userId, UUID deviceId,
                                          String deviceName, String macAddress) {
        License license = licenseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("License not found"));

        if (Boolean.TRUE.equals(license.getBlocked())) {
            throw new RuntimeException("License is blocked");
        }
        if (!license.getEndingDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("License expired");
        }

        long activatedDevices = deviceLicenseRepository.countByLicenseId(license.getId());
        if (activatedDevices >= license.getDeviceCount()) {
            throw new RuntimeException("Device limit exceeded");
        }

        boolean deviceLicenseExists = deviceLicenseRepository
                .findByLicenseIdAndDeviceId(license.getId(), deviceId)
                .isPresent();

        if (!deviceLicenseExists) {
            if (!deviceRepository.existsById(deviceId)) {
                Device device = new Device();
                device.setUserId(userId);
                device.setName(deviceName);
                device.setMacAddress(macAddress);
                deviceRepository.save(device);
            }

            DeviceLicense deviceLicense = new DeviceLicense();
            deviceLicense.setLicenseId(license.getId());
            deviceLicense.setDeviceId(deviceId);
            deviceLicense.setActivationDate(LocalDate.now());
            deviceLicenseRepository.save(deviceLicense);
        }

        if (license.getFirstActivationDate() == null) {
            license.setFirstActivationDate(LocalDate.now());
            licenseRepository.save(license);
        }

        saveHistory(license.getId(), userId, "ACTIVATED");

        return buildTicketResponse(license, userId, deviceId);
    }

    public TicketResponse checkLicense(String code, UUID deviceId) {
        License license = licenseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("License not found"));

        deviceLicenseRepository.findByLicenseIdAndDeviceId(license.getId(), deviceId)
                .orElseThrow(() -> new RuntimeException("Device is not activated for this license"));

        if (Boolean.TRUE.equals(license.getBlocked())) {
            throw new RuntimeException("License is blocked");
        }
        if (!license.getEndingDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("License expired");
        }

        return buildTicketResponse(license, license.getUserId(), deviceId);
    }

    @Transactional
    public License renewLicense(UUID licenseId, UUID userId) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new RuntimeException("License not found"));

        LicenseType type = licenseTypeRepository.findById(license.getTypeId())
                .orElseThrow(() -> new RuntimeException("License type not found"));

        license.setEndingDate(license.getEndingDate().plusDays(type.getDefaultDurationInDays()));
        License saved = licenseRepository.save(license);

        saveHistory(licenseId, userId, "RENEWED");

        return saved;
    }

    private void saveHistory(UUID licenseId, UUID userId, String status) {
        LicenseHistory history = new LicenseHistory();
        history.setLicenseId(licenseId);
        history.setUserId(userId);
        history.setStatus(status);
        history.setChangeDate(LocalDateTime.now());
        licenseHistoryRepository.save(history);
    }

    private TicketResponse buildTicketResponse(License license, UUID userId, UUID deviceId) {
        Ticket ticket = new Ticket(
                LocalDateTime.now(),
                license.getFirstActivationDate(),
                license.getEndingDate(),
                userId,
                deviceId,
                license.getBlocked()
        );

        TicketResponse response = new TicketResponse();
        response.setTicket(ticket);
        response.setDigitalSignature("STUB");
        return response;
    }
}
