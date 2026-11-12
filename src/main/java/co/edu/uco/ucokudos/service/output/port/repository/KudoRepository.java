package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.Kudo;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface KudoRepository {

    Kudo save(Kudo kudo);

    Optional<Kudo> findById(UUID id);

    long countByAuthorBetween(UUID authorId, Instant from, Instant to);
}
