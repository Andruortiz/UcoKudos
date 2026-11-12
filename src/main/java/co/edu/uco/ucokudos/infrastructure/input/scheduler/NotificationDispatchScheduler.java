package co.edu.uco.ucokudos.infrastructure.input.scheduler;

import co.edu.uco.ucokudos.service.input.port.DispatchNotificationsPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kudos.dispatch.enabled", havingValue = "true")
public class NotificationDispatchScheduler {

    private final DispatchNotificationsPort dispatchNotificationsPort;

    public NotificationDispatchScheduler(final DispatchNotificationsPort dispatchNotificationsPort) {
        this.dispatchNotificationsPort = dispatchNotificationsPort;
    }

    @Scheduled(fixedDelayString = "${kudos.dispatch.interval}")
    public void run() {
        dispatchNotificationsPort.execute();
    }
}
