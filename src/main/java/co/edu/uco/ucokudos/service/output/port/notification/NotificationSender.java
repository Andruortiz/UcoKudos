package co.edu.uco.ucokudos.service.output.port.notification;

import co.edu.uco.ucokudos.domain.model.NotificationChannel;

public interface NotificationSender {

    NotificationChannel channel();

    DeliveryResult send(KudoNotice notice);
}
