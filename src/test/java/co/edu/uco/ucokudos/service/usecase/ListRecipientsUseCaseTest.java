package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.KudoDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.service.input.dto.request.ListRecipientsRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ListRecipientsResponse;
import co.edu.uco.ucokudos.service.input.dto.response.RecipientResponse;
import co.edu.uco.ucokudos.service.output.port.repository.RecipientDirectoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListRecipientsUseCaseTest {

    static class FakeDirectory implements RecipientDirectoryRepository {
        List<User> recent = List.of();
        List<User> shared = List.of();
        List<User> leadership = List.of();
        List<User> search = List.of();
        final List<String> calls = new ArrayList<>();
        Collection<Role> lastRoles;

        public List<User> findRecent(final UUID authorId, final Collection<Role> roles, final int limit) {
            calls.add("recent");
            lastRoles = roles;
            return recent;
        }

        public List<User> findInSharedPrograms(final UUID authorId, final Collection<Role> roles, final String text, final int limit) {
            calls.add("shared");
            return shared;
        }

        public List<User> findLeadership(final UUID authorId, final Collection<Role> roles, final String text, final int limit) {
            calls.add("leadership");
            return leadership;
        }

        public List<User> search(final UUID authorId, final Collection<Role> roles, final String text, final int limit) {
            calls.add("search");
            return search;
        }
    }

    private final FakeDirectory directory = new FakeDirectory();
    private User requester;
    private ListRecipientsUseCase useCase;

    private User user(final Role role, final String name) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setName(name);
        user.setEmail(name + "@uco.net.co");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private ListRecipientsUseCase build(final int limit) {
        return new ListRecipientsUseCase(id -> Optional.of(requester), directory, new UserDomain(), new KudoDomain(),
                limit, 10);
    }

    private ListRecipientsResponse run(final String text) {
        final ListRecipientsRequest request = new ListRecipientsRequest();
        request.setRequesterId(requester.getId());
        request.setText(text);
        return useCase.execute(request);
    }

    private List<String> names(final ListRecipientsResponse response) {
        return response.getItems().stream().map(RecipientResponse::getName).toList();
    }

    @BeforeEach
    void setUp() {
        requester = user(Role.STUDENT, "autor");
        useCase = build(20);
    }

    @Test
    void withoutTextItListsRecentThenProgramsThenLeadershipWithoutRepeats() {
        final User ana = user(Role.STUDENT, "ana");
        final User luis = user(Role.TEACHER, "luis");
        final User coordinator = user(Role.PROGRAM_COORDINATOR, "coord");
        directory.recent = List.of(luis);
        directory.shared = List.of(ana, luis);
        directory.leadership = List.of(coordinator, ana);

        final ListRecipientsResponse response = run(null);

        assertEquals(List.of("luis", "ana", "coord"), names(response));
        assertEquals(List.of("recent", "shared", "leadership"), directory.calls);
    }

    @Test
    void withTextItSearchesTheWholeUniversityAndSkipsRecent() {
        final User far = user(Role.STUDENT, "carlos");
        directory.search = List.of(far);

        final ListRecipientsResponse response = run("  car ");

        assertEquals(List.of("carlos"), names(response));
        assertEquals(List.of("shared", "leadership", "search"), directory.calls);
    }

    @Test
    void tooShortTextIsRejected() {
        assertEquals(MessageCodes.RECIPIENT_TEXT_TOO_SHORT,
                assertThrows(DomainException.class, () -> run("ab")).getCode());
        assertTrue(directory.calls.isEmpty());
    }

    @Test
    void theRolesSentToTheDirectoryFollowTheMatrix() {
        run(null);
        assertEquals(java.util.Set.of(Role.STUDENT, Role.TEACHER), java.util.Set.copyOf(directory.lastRoles));
    }

    @Test
    void anAdministratorGetsAnEmptyListWithoutQueryingTheDirectory() {
        requester = user(Role.ADMINISTRATOR, "admin");
        assertTrue(run(null).getItems().isEmpty());
        assertTrue(directory.calls.isEmpty());
    }

    @Test
    void theResultIsCapped() {
        useCase = build(2);
        directory.shared = List.of(user(Role.STUDENT, "a1"), user(Role.STUDENT, "a2"), user(Role.STUDENT, "a3"));
        assertEquals(2, run(null).getItems().size());
    }

    @Test
    void inactiveRequesterIsRejected() {
        requester.setStatus(UserStatus.INACTIVE);
        assertEquals(MessageCodes.USER_NOT_ACTIVE, assertThrows(DomainException.class, () -> run(null)).getCode());
    }

    @Test
    void unknownRequesterIsRejected() {
        useCase = new ListRecipientsUseCase(id -> Optional.empty(), directory, new UserDomain(), new KudoDomain(), 20, 10);
        assertEquals(MessageCodes.USER_NOT_FOUND, assertThrows(DomainException.class, () -> run(null)).getCode());
    }
}
