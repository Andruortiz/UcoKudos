package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.service.input.port.RequestAccessPort;
import co.edu.uco.ucokudos.service.output.port.directory.DirectoryInviter;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import co.edu.uco.ucokudos.service.usecase.RequestAccessUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AccessConfiguration {

    @Bean
    RequestAccessPort requestAccessPort(
            final UserRepository users,
            final UserDomain userDomain,
            final DirectoryInviter inviter,
            @Value("${kudos.registration.allowed-email-domains}") final List<String> allowedDomains) {
        return new RequestAccessUseCase(users, userDomain, allowedDomains, inviter);
    }
}
