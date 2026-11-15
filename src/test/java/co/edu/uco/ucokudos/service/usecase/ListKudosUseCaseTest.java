package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.KudoDirection;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.request.ListKudosRequest;
import co.edu.uco.ucokudos.service.input.dto.response.KudoResponse;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListKudosUseCaseTest {

    private final User ana = user("Ana");
    private final User luis = user("Luis");
    private final Category empathy = category("Empatía");
    private final UsersStub users = new UsersStub(id -> Optional.ofNullable(
            Map.of(ana.getId(), ana, luis.getId(), luis).get(id)));
    private int userQueries;

    private static User user(final String name) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setName(name);
        return user;
    }

    private static Category category(final String name) {
        final Category category = new Category();
        category.setId(UuidHelper.generate());
        category.setName(name);
        return category;
    }

    private Kudo kudo(final UUID categoryId) {
        final Kudo kudo = new Kudo();
        kudo.setId(UuidHelper.generate());
        kudo.setAuthorId(ana.getId());
        kudo.setRecipientId(luis.getId());
        kudo.setCategoryId(categoryId);
        kudo.setContent("Gracias");
        kudo.setCreatedAt(Instant.parse("2026-11-14T10:00:00Z"));
        return kudo;
    }

    private ListKudosUseCase useCase(final List<Kudo> kudos) {
        final KudoRepository kudoRepository = new KudoRepository() {
            public Kudo save(final Kudo kudo) { return kudo; }
            public Optional<Kudo> findById(final UUID id) { return Optional.empty(); }
            public long countByAuthorBetween(final UUID authorId, final Instant from, final Instant to) { return 0; }
            public List<Kudo> findAllByFilters(final UUID userId, final UUID categoryId, final KudoDirection direction) {
                return kudos;
            }
        };
        final CategoryRepository categories = new CategoryRepository() {
            public Optional<Category> findById(final UUID id) { return Optional.empty(); }
            public List<Category> findActive() { return List.of(empathy); }
            public List<Category> findByIds(final Collection<UUID> ids) { return List.of(empathy); }
        };
        return new ListKudosUseCase(kudoRepository, new UsersStub(id -> {
            userQueries++;
            return users.findById(id);
        }) {
            @Override
            public List<User> findByIds(final Collection<UUID> ids) {
                userQueries++;
                return users.findByIds(ids);
            }
        }, categories);
    }

    @Test
    void eachKudoCarriesTheNamesOfItsAuthorRecipientAndCategory() {
        final List<KudoResponse> items = useCase(List.of(kudo(empathy.getId())))
                .execute(new ListKudosRequest()).getItems();

        assertEquals(1, items.size());
        assertEquals("Ana", items.get(0).getAuthorName());
        assertEquals("Luis", items.get(0).getRecipientName());
        assertEquals("Empatía", items.get(0).getCategoryName());
    }

    @Test
    void usersAreLoadedOnceForTheWholePage() {
        useCase(List.of(kudo(empathy.getId()), kudo(empathy.getId()), kudo(empathy.getId())))
                .execute(new ListKudosRequest());

        assertEquals(1, userQueries);
    }

    @Test
    void noKudosGivesAnEmptyList() {
        assertTrue(useCase(List.of()).execute(new ListKudosRequest()).getItems().isEmpty());
    }
}
