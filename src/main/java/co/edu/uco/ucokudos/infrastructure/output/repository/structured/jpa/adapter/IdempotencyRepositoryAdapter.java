package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.IdempotencyRecord;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.IdempotencyJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.IdempotencyEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.IdempotencyJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.IdempotencyRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyRepositoryAdapter implements IdempotencyRepository {

    private final IdempotencyJpaRepository jpaRepository;
    private final IdempotencyEntityMapper mapper;

    public IdempotencyRepositoryAdapter(
            final IdempotencyJpaRepository jpaRepository,
            final IdempotencyEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<IdempotencyRecord> find(final String key, final UUID authorId, final Instant since) {
        return jpaRepository.findByKeyAndAuthorIdAndCreatedAtGreaterThanEqual(key, authorId, since)
                .map(mapper::toDomain);
    }

    @Override
    public void save(final IdempotencyRecord record) {
        final IdempotencyJpaEntity entity = mapper.toJpa(record);
        entity.setId(jpaRepository.findByKeyAndAuthorId(record.key(), record.authorId())
                .map(IdempotencyJpaEntity::getId)
                .orElseGet(UuidHelper::generate));
        jpaRepository.save(entity);
    }
}
