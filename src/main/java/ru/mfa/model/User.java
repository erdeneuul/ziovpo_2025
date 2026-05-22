package ru.mfa.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * WHAT IS THIS?
 * A "User" is a person who can log in to our application.
 * We store users in the PostgreSQL database in a table called "users".
 *
 * Each user has:
 *   - id: unique number
 *   - email: used as username to log in
 *   - password: stored as a HASH (never plain text!)
 *   - role: either ROLE_USER or ROLE_ADMIN
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // stored as BCrypt hash, NEVER plain text

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
