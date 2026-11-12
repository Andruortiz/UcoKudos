package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.NotificationEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.NotificationJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationEntityMapper mapper;

    public NotificationRepositoryAdapter(
            final NotificationJpaRepository jpaRepository,
            final NotificationEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void saveAll(final List<Notification> notifications) {
        jpaRepository.saveAll(notifications.stream().map(mapper::toJpa).toList());
    }

    @Override
    public List<Notification> findByKudoId(final UUID kudoId) {
        return jpaRepository.findByKudoId(kudoId).stream().map(mapper::toDomain).toList();
    }
}
