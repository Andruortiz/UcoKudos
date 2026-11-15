package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.KudoDirection;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.response.KudoDetailResponse;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetKudoUseCaseTest {

    private final User ana = user("Ana");
    private final User luis = user("Luis");
    private final Category empathy = new Category();
    private final Kudo kudo = new Kudo();
    private final GetKudoUseCase useCase;

    GetKudoUseCaseTest() {
        empathy.setId(UuidHelper.generate());
        empathy.setName("Empatía");
        kudo.setId(UuidHelper.generate());
        kudo.setAuthorId(ana.getId());
        kudo.setRecipientId(luis.getId());
        kudo.setCategoryId(empathy.getId());
        kudo.setContent("Gracias por tu ayuda");
        kudo.setCreatedAt(Instant.parse("2026-11-14T10:00:00Z"));
        kudo.setAttachments(List.of(new Attachment("nota.pdf", "application/pdf", new byte[]{1, 2, 3})));

        final KudoRepository kudos = new KudoRepository() {
            public Kudo save(final Kudo value) { return value; }
            public Optional<Kudo> findById(final UUID id) {
                return id.equals(kudo.getId()) ? Optional.of(kudo) : Optional.empty();
            }
            public long countByAuthorBetween(final UUID authorId, final Instant from, final Instant to) { return 0; }
            public List<Kudo> findAllByFilters(final UUID userId, final UUID categoryId, final KudoDirection d) {
                return List.of();
            }
        };
        final CategoryRepository categories = new CategoryRepository() {
            public Optional<Category> findById(final UUID id) { return Optional.of(empathy); }
            public List<Category> findActive() { return List.of(empathy); }
            public List<Category> findByIds(final Collection<UUID> ids) { return List.of(empathy); }
        };
        final Map<UUID, User> byId = Map.of(ana.getId(), ana, luis.getId(), luis);
        useCase = new GetKudoUseCase(kudos, new UsersStub(id -> Optional.ofNullable(byId.get(id))), categories);
    }

    private static User user(final String name) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setName(name);
        return user;
    }

    private String codeOf(final UUID requester, final UUID kudoId) {
        return assertThrows(DomainException.class, () -> useCase.execute(requester, kudoId)).getCode();
    }

    @Test
    void theAuthorSeesTheWholeKudoWithNamesAndAttachmentData() {
        final KudoDetailResponse detail = useCase.execute(ana.getId(), kudo.getId());

        assertEquals("Ana", detail.getAuthorName());
        assertEquals("Luis", detail.getRecipientName());
        assertEquals("Empatía", detail.getCategoryName());
        assertEquals("Gracias por tu ayuda", detail.getContent());
        assertEquals(1, detail.getAttachments().size());
        assertEquals("nota.pdf", detail.getAttachments().get(0).name());
        assertEquals(3, detail.getAttachments().get(0).size());
    }

    @Test
    void theRecipientSeesTheKudoToo() {
        assertEquals(kudo.getId(), useCase.execute(luis.getId(), kudo.getId()).getId());
    }

    @Test
    void aStrangerGetsTheSameAnswerAsForAMissingKudo() {
        assertEquals(MessageCodes.KUDO_NOT_FOUND, codeOf(UuidHelper.generate(), kudo.getId()));
    }

    @Test
    void aMissingKudoIsNotFound() {
        assertEquals(MessageCodes.KUDO_NOT_FOUND, codeOf(ana.getId(), UuidHelper.generate()));
    }
}
