package ru.mfa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mfa.model.DeviceLicense;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, UUID> {
    Optional<DeviceLicense> findByLicenseIdAndDeviceId(UUID licenseId, UUID deviceId);
    long countByLicenseId(UUID licenseId);
}
