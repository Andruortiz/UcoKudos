package co.edu.uco.ucokudos.domain.model;

import java.time.Instant;
import java.util.UUID;

/** A person asking for an account; it stays pending until someone above them approves it (ADR-0004). */
public class RegistrationRequest {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private RegistrationStatus status = RegistrationStatus.PENDING;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(final String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(final String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(final Role role) { this.role = role; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(final RegistrationStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }
}
