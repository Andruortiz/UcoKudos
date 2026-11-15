package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.service.input.port.ListKudosPort;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.usecase.ListKudosUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListKudosConfiguration {

    @Bean
    ListKudosPort listKudosPort(final KudoRepository kudo) {
        return new ListKudosUseCase(kudo);
    }
}
