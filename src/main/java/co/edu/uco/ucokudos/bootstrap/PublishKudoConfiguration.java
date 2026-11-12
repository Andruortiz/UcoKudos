package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.domain.KudoDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.service.input.port.PublishKudoPort;
import co.edu.uco.ucokudos.service.output.port.attachment.AttachmentProcessor;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.IdempotencyRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.output.port.repository.NotificationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import co.edu.uco.ucokudos.service.usecase.PublishKudoUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class PublishKudoConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    UserDomain userDomain() {
        return new UserDomain();
    }

    @Bean
    KudoDomain kudoDomain() {
        return new KudoDomain();
    }

    @Bean
    PublishKudoPort publishKudoPort(
            final UserRepository users,
            final CategoryRepository categories,
            final KudoRepository kudos,
            final NotificationRepository notifications,
            final AuditRepository audits,
            final IdempotencyRepository idempotency,
            final AttachmentProcessor attachmentProcessor,
            final UserDomain userDomain,
            final KudoDomain kudoDomain,
            final Clock clock,
            @Value("${kudos.daily-limit}") final int dailyLimit,
            @Value("${kudos.idempotency-window}") final Duration idempotencyWindow) {
        return new PublishKudoUseCase(users, categories, kudos, notifications, audits, idempotency,
                attachmentProcessor, userDomain, kudoDomain, clock, dailyLimit, idempotencyWindow);
    }
}
