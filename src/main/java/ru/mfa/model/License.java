package ru.mfa.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "licenses")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "type_id", nullable = false)
    private UUID typeId;

    @Column(name = "first_activation_date")
    private LocalDate firstActivationDate;

    @Column(name = "ending_date")
    private LocalDate endingDate;

    @Column(nullable = false)
    private Boolean blocked;

    @Column(name = "device_count", nullable = false)
    private Integer deviceCount;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column
    private String description;

    public License() {}

    public UUID getId() { return id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getTypeId() { return typeId; }
    public void setTypeId(UUID typeId) { this.typeId = typeId; }

    public LocalDate getFirstActivationDate() { return firstActivationDate; }
    public void setFirstActivationDate(LocalDate firstActivationDate) { this.firstActivationDate = firstActivationDate; }

    public LocalDate getEndingDate() { return endingDate; }
    public void setEndingDate(LocalDate endingDate) { this.endingDate = endingDate; }

    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }

    public Integer getDeviceCount() { return deviceCount; }
    public void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
