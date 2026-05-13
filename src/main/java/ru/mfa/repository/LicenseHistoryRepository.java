package ru.mfa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mfa.model.LicenseHistory;

import java.util.UUID;

@Repository
public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, UUID> {
}
