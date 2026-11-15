package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    void saveAll(List<Notification> notifications);

    void save(Notification notification);

    List<Notification> findByKudoId(UUID kudoId);

    List<Notification> findDue(Collection<NotificationChannel> channels, Instant now, int limit);
}
