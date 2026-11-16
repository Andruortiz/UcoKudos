package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.service.input.dto.response.ListCategoriesResponse;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListCategoriesUseCaseTest {

    private static Category category(final String name) {
        final Category category = new Category();
        category.setId(UuidHelper.generate());
        category.setName(name);
        return category;
    }

    private static ListCategoriesUseCase useCaseWith(final List<Category> active) {
        return new ListCategoriesUseCase(new CategoryRepository() {
            public Optional<Category> findById(final UUID id) { return Optional.empty(); }
            public List<Category> findActive() { return active; }
            public List<Category> findByIds(final Collection<UUID> ids) { return List.of(); }
        });
    }

    @Test
    void itListsTheActiveCategoriesInTheOrderTheRepositoryGivesThem() {
        final Category empathy = category("Empatía");
        final Category leadership = category("Liderazgo");

        final ListCategoriesResponse response = useCaseWith(List.of(empathy, leadership)).execute();

        assertEquals(2, response.getItems().size());
        assertEquals(empathy.getId(), response.getItems().get(0).getId());
        assertEquals("Empatía", response.getItems().get(0).getName());
        assertEquals("Liderazgo", response.getItems().get(1).getName());
    }

    @Test
    void withoutActiveCategoriesTheListIsEmptyNotNull() {
        final ListCategoriesResponse response = useCaseWith(List.of()).execute();

        assertTrue(response.getItems().isEmpty());
    }
}
