package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    void saveAll(List<Notification> notifications);

    List<Notification> findByKudoId(UUID kudoId);
}
