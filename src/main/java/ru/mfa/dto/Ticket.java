package ru.mfa.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {

    private LocalDateTime serverDate;
    private long ticketLifetime = 3600;
    private LocalDate activationDate;
    private LocalDate expirationDate;
    private UUID userId;
    private UUID deviceId;
    private Boolean isBlocked;

    public Ticket() {}

    public Ticket(LocalDateTime serverDate, LocalDate activationDate, LocalDate expirationDate,
                  UUID userId, UUID deviceId, Boolean isBlocked) {
        this.serverDate = serverDate;
        this.activationDate = activationDate;
        this.expirationDate = expirationDate;
        this.userId = userId;
        this.deviceId = deviceId;
        this.isBlocked = isBlocked;
    }

    public LocalDateTime getServerDate() { return serverDate; }
    public void setServerDate(LocalDateTime serverDate) { this.serverDate = serverDate; }

    public long getTicketLifetime() { return ticketLifetime; }
    public void setTicketLifetime(long ticketLifetime) { this.ticketLifetime = ticketLifetime; }

    public LocalDate getActivationDate() { return activationDate; }
    public void setActivationDate(LocalDate activationDate) { this.activationDate = activationDate; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }
}
