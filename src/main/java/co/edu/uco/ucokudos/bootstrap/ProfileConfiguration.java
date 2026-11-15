package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.service.input.port.GetProfilePort;
import co.edu.uco.ucokudos.service.input.port.UpdatePhonePort;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import co.edu.uco.ucokudos.service.usecase.GetProfileUseCase;
import co.edu.uco.ucokudos.service.usecase.UpdatePhoneUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ProfileConfiguration {

    @Bean
    GetProfilePort getProfilePort(final UserRepository users) {
        return new GetProfileUseCase(users);
    }

    @Bean
    UpdatePhonePort updatePhonePort(final UserRepository users, final AuditRepository audits,
                                    final UserDomain userDomain, final Clock clock) {
        return new UpdatePhoneUseCase(users, audits, userDomain, clock);
    }
}
