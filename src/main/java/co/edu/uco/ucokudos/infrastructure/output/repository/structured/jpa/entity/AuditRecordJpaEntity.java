package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity;

import co.edu.uco.ucokudos.domain.model.AuditAction;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_record",
        indexes = @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id"))
public class AuditRecordJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_id", nullable = true)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 40)
    private AuditAction action;

    @Column(name = "resource_type", nullable = false, length = 40)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(final Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(final UUID actorId) {
        this.actorId = actorId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(final AuditAction action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(final UUID resourceId) {
        this.resourceId = resourceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(final String correlationId) {
        this.correlationId = correlationId;
    }
}
