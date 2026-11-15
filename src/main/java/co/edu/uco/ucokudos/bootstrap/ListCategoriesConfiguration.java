package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.service.input.port.ListCategoriesPort;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.usecase.ListCategoriesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListCategoriesConfiguration {

    @Bean
    ListCategoriesPort listCategoriesPort(final CategoryRepository categories) {
        return new ListCategoriesUseCase(categories);
    }
}
