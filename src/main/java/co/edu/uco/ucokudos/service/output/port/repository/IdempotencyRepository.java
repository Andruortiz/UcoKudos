package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.IdempotencyRecord;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepository {

    Optional<IdempotencyRecord> find(String key, UUID authorId, Instant since);

    void save(IdempotencyRecord record);
}
