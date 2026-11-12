package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.IdempotencyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyJpaEntity, UUID> {

    Optional<IdempotencyJpaEntity> findByKeyAndAuthorId(String key, UUID authorId);

    Optional<IdempotencyJpaEntity> findByKeyAndAuthorIdAndCreatedAtGreaterThanEqual(String key, UUID authorId, Instant since);
}
