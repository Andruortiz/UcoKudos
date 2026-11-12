package co.edu.uco.ucokudos.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Category {

    private UUID id;
    private String name;
    private boolean active = true;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }
}
