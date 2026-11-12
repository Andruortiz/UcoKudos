package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.KudoAttachmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KudoAttachmentJpaRepository extends JpaRepository<KudoAttachmentJpaEntity, UUID> {

    List<KudoAttachmentJpaEntity> findByKudoId(UUID kudoId);
}
