package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.domain.KudoDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.service.input.port.ListRecipientsPort;
import co.edu.uco.ucokudos.service.output.port.repository.RecipientDirectoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import co.edu.uco.ucokudos.service.usecase.ListRecipientsUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListRecipientsConfiguration {

    @Bean
    ListRecipientsPort listRecipientsPort(
            final UserRepository users,
            final RecipientDirectoryRepository directory,
            final UserDomain userDomain,
            final KudoDomain kudoDomain,
            @Value("${kudos.recipients.limit}") final int limit,
            @Value("${kudos.recipients.recent-limit}") final int recentLimit) {
        return new ListRecipientsUseCase(users, directory, userDomain, kudoDomain, limit, recentLimit);
    }
}
