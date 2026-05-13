package ru.mfa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mfa.model.LicenseType;

import java.util.UUID;

@Repository
public interface LicenseTypeRepository extends JpaRepository<LicenseType, UUID> {
}
