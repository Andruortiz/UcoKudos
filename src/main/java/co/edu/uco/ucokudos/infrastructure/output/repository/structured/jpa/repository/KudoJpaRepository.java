package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.KudoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KudoJpaRepository extends JpaRepository<KudoJpaEntity, UUID> {

    long countByAuthorIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID authorId, Instant from, Instant to);
}
