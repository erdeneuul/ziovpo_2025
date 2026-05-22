package ru.mfa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mfa.dto.AuthDtos.LicenseTicketDto;
import ru.mfa.exception.*;
import ru.mfa.model.License;
import ru.mfa.model.LicenseStatus;
import ru.mfa.model.User;
import ru.mfa.repository.LicenseRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;

    @Transactional
    public LicenseTicketDto activate(String activationCode, User user) {
        License license = licenseRepository.findByActivationCode(activationCode)
                .orElseThrow(() -> new LicenseNotFoundException("Activation code not found"));

        if (license.getUser() != null && !license.getUser().getId().equals(user.getId())) {
            throw new LicenseAlreadyUsedException();
        }

        if (license.getStatus() == LicenseStatus.BLOCKED) {
            throw new LicenseBlockedException();
        }

        if (license.getStatus() == LicenseStatus.EXPIRED
                || license.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new LicenseExpiredException(license.getExpiresAt());
        }

        license.setUser(user);
        license.setStatus(LicenseStatus.ACTIVE);
        license.setActivatedAt(LocalDateTime.now());
        licenseRepository.save(license);

        return toDto(license);
    }

    @Transactional
    public LicenseTicketDto getStatus(User user) {
        License license = licenseRepository.findByUser(user)
                .orElseThrow(() -> new LicenseNotFoundException("No license found"));

        if (license.getExpiresAt().isBefore(LocalDateTime.now())) {
            license.setStatus(LicenseStatus.EXPIRED);
            licenseRepository.save(license);
            throw new LicenseExpiredException(license.getExpiresAt());
        }

        if (license.getStatus() == LicenseStatus.BLOCKED) {
            throw new LicenseBlockedException();
        }

        return toDto(license);
    }

    private LicenseTicketDto toDto(License license) {
        String email = license.getUser() != null ? license.getUser().getEmail() : null;
        return new LicenseTicketDto(email, license.getStatus(),
                license.getExpiresAt(), license.getActivatedAt());
    }
}
