package ru.mfa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mfa.model.User;

import java.util.Optional;

/**
 * WHAT IS A REPOSITORY?
 * It's the layer that talks to the database.
 * Spring automatically generates SQL queries from method names.
 *
 * findByEmail("alice@example.com")
 *   → SELECT * FROM users WHERE email = 'alice@example.com'
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
