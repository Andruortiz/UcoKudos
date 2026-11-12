package co.edu.uco.ucokudos.domain.model;

import java.time.Instant;
import java.util.UUID;

public record IdempotencyRecord(String key, UUID authorId, String requestHash, UUID kudoId, Instant createdAt) {
}
