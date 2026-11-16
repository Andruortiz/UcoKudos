package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.service.input.port.GetAttachmentPort;
import co.edu.uco.ucokudos.service.input.port.GetKudoPort;
import co.edu.uco.ucokudos.service.output.port.attachment.AttachmentStorage;
import co.edu.uco.ucokudos.service.usecase.GetAttachmentUseCase;
import co.edu.uco.ucokudos.service.input.port.ListKudosPort;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import co.edu.uco.ucokudos.service.usecase.GetKudoUseCase;
import co.edu.uco.ucokudos.service.usecase.ListKudosUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListKudosConfiguration {

    @Bean
    ListKudosPort listKudosPort(final KudoRepository kudos, final UserRepository users,
                                final CategoryRepository categories) {
        return new ListKudosUseCase(kudos, users, categories);
    }

    @Bean
    GetKudoPort getKudoPort(final KudoRepository kudos, final UserRepository users,
                            final CategoryRepository categories) {
        return new GetKudoUseCase(kudos, users, categories);
    }

    @Bean
    GetAttachmentPort getAttachmentPort(final KudoRepository kudos, final AttachmentStorage storage) {
        return new GetAttachmentUseCase(kudos, storage);
    }
}
