package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.service.input.port.SubmitRegistrationPort;
import co.edu.uco.ucokudos.service.output.port.repository.RegistrationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import co.edu.uco.ucokudos.service.usecase.SubmitRegistrationUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class RegistrationConfiguration {

    @Bean
    SubmitRegistrationPort submitRegistrationPort(
            final RegistrationRepository registrations,
            final UserRepository users,
            final UserDomain userDomain,
            final Clock clock,
            @Value("${kudos.registration.allowed-email-domains}") final List<String> allowedDomains) {
        return new SubmitRegistrationUseCase(registrations, users, userDomain, allowedDomains, clock);
    }
}
