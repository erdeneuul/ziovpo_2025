package ru.mfa.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mfa.exception.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Without this class, errors look like ugly Java stack traces.
 * With this class, errors look like clean JSON:
 *
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Email is already registered",
 *   "timestamp": "2026-05-12T10:00:00Z"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles @Valid validation failures (e.g. blank email, weak password)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return error(HttpStatus.BAD_REQUEST, errors);
    }

    // Handles business logic errors (wrong password, email taken, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegal(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(LicenseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleLicenseNotFound(LicenseNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LicenseAlreadyUsedException.class)
    public ResponseEntity<Map<String, Object>> handleLicenseAlreadyUsed(LicenseAlreadyUsedException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LicenseExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleLicenseExpired(LicenseExpiredException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", 402);
        body.put("error", "Payment Required");
        body.put("message", ex.getMessage());
        body.put("expiresAt", ex.getExpiresAt().toString());
        return ResponseEntity.status(402).body(body);
    }

    @ExceptionHandler(LicenseBlockedException.class)
    public ResponseEntity<Map<String, Object>> handleLicenseBlocked(LicenseBlockedException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
