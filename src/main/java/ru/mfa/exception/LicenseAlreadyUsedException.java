package ru.mfa.exception;

public class LicenseAlreadyUsedException extends RuntimeException {
    public LicenseAlreadyUsedException() {
        super("Activation code already used");
    }
}
