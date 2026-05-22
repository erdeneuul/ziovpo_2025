package ru.mfa.exception;

public class LicenseBlockedException extends RuntimeException {
    public LicenseBlockedException() {
        super("License is blocked");
    }
}
