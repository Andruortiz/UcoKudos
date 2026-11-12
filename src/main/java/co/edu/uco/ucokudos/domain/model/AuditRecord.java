package co.edu.uco.ucokudos.domain.model;

import java.time.Instant;
import java.util.UUID;

public record AuditRecord(UUID id, Instant occurredAt, UUID actorId, AuditAction action,
                          String resourceType, UUID resourceId, String correlationId) {
}
