package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.KudoAttachmentJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.KudoAttachmentEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.KudoEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.KudoAttachmentJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.KudoJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class KudoRepositoryAdapter implements KudoRepository {

    private final KudoJpaRepository jpaRepository;
    private final KudoAttachmentJpaRepository attachmentRepository;
    private final KudoEntityMapper mapper;
    private final KudoAttachmentEntityMapper attachmentMapper;

    public KudoRepositoryAdapter(
            final KudoJpaRepository jpaRepository,
            final KudoAttachmentJpaRepository attachmentRepository,
            final KudoEntityMapper mapper,
            final KudoAttachmentEntityMapper attachmentMapper) {
        this.jpaRepository = jpaRepository;
        this.attachmentRepository = attachmentRepository;
        this.mapper = mapper;
        this.attachmentMapper = attachmentMapper;
    }

    @Override
    @Transactional
    public Kudo save(final Kudo kudo) {
        jpaRepository.save(mapper.toJpa(kudo));
        final List<KudoAttachmentJpaEntity> attachments = kudo.getAttachments().stream()
                .map(attachment -> {
                    final KudoAttachmentJpaEntity entity = attachmentMapper.toJpa(attachment, kudo.getId());
                    entity.setId(UuidHelper.generate());
                    return entity;
                })
                .toList();
        attachmentRepository.saveAll(attachments);
        return kudo;
    }

    @Override
    public Optional<Kudo> findById(final UUID id) {
        return jpaRepository.findById(id).map(entity -> {
            final Kudo kudo = mapper.toDomain(entity);
            kudo.setAttachments(attachmentRepository.findByKudoId(id).stream()
                    .map(attachmentMapper::toDomain)
                    .toList());
            return kudo;
        });
    }

    @Override
    public long countByAuthorBetween(final UUID authorId, final Instant from, final Instant to) {
        return jpaRepository.countByAuthorIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(authorId, from, to);
    }
}
