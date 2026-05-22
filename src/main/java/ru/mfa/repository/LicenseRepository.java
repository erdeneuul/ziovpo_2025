package ru.mfa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mfa.model.License;
import ru.mfa.model.User;

import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByActivationCode(String activationCode);
    Optional<License> findByUser(User user);
}
