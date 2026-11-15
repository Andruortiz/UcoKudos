package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.NotificationJpaEntity;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.domain.model.NotificationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByKudoId(UUID kudoId);

    List<NotificationJpaEntity> findByStatusAndChannelInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            NotificationStatus status, Collection<NotificationChannel> channels, Instant now, Pageable pageable);
}
