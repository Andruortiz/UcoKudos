package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.domain.NotificationDomain;
import co.edu.uco.ucokudos.service.input.port.DispatchNotificationsPort;
import co.edu.uco.ucokudos.service.output.port.notification.NotificationSender;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.output.port.repository.NotificationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import co.edu.uco.ucokudos.service.usecase.DispatchNotificationsUseCase;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableScheduling
public class DispatchNotificationsConfiguration {

    @Bean
    NotificationDomain notificationDomain() {
        return new NotificationDomain();
    }

    @Bean
    DispatchNotificationsPort dispatchNotificationsPort(
            final NotificationRepository notifications,
            final KudoRepository kudos,
            final UserRepository users,
            final CategoryRepository categories,
            final AuditRepository audits,
            final NotificationDomain notificationDomain,
            final ObjectProvider<NotificationSender> senders,
            final Clock clock,
            @Value("${kudos.dispatch.batch-size}") final int batchSize,
            @Value("${kudos.dispatch.max-attempts}") final int maxAttempts,
            @Value("${kudos.dispatch.base-delay}") final Duration baseDelay,
            @Value("${kudos.dispatch.link-base-url}") final String linkBaseUrl) {
        return new DispatchNotificationsUseCase(notifications, kudos, users, categories, audits, notificationDomain,
                senders.stream().toList(), clock, batchSize, maxAttempts, baseDelay, linkBaseUrl);
    }
}
