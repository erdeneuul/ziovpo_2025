package ru.mfa.model;

/**
 * WHAT IS A ROLE?
 * A role defines WHAT a user is allowed to do.
 *
 * ROLE_USER  → regular user, limited access
 * ROLE_ADMIN → administrator, full access
 *
 * Spring Security requires the "ROLE_" prefix.
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
