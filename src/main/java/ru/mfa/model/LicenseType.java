package ru.mfa.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "license_types")
public class LicenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_duration_in_days")
    private Integer defaultDurationInDays;

    @Column
    private String description;

    public LicenseType() {}

    public UUID getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDefaultDurationInDays() { return defaultDurationInDays; }
    public void setDefaultDurationInDays(Integer defaultDurationInDays) { this.defaultDurationInDays = defaultDurationInDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
