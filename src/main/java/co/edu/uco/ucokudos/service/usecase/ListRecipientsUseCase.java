package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.KudoDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.request.ListRecipientsRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ListRecipientsResponse;
import co.edu.uco.ucokudos.service.input.dto.response.RecipientResponse;
import co.edu.uco.ucokudos.service.input.port.ListRecipientsPort;
import co.edu.uco.ucokudos.service.output.port.repository.RecipientDirectoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Transactional(readOnly = true)
public class ListRecipientsUseCase implements ListRecipientsPort {

    public static final int MIN_TEXT_LENGTH = 3;

    private final UserRepository users;
    private final RecipientDirectoryRepository directory;
    private final UserDomain userDomain;
    private final KudoDomain kudoDomain;
    private final int limit;
    private final int recentLimit;

    public ListRecipientsUseCase(final UserRepository users, final RecipientDirectoryRepository directory,
                                 final UserDomain userDomain, final KudoDomain kudoDomain,
                                 final int limit, final int recentLimit) {
        this.users = users;
        this.directory = directory;
        this.userDomain = userDomain;
        this.kudoDomain = kudoDomain;
        this.limit = limit;
        this.recentLimit = recentLimit;
    }

    @Override
    public ListRecipientsResponse execute(final ListRecipientsRequest request) {
        final User requester = users.findById(request.getRequesterId())
                .orElseThrow(() -> new DomainException(MessageCodes.USER_NOT_FOUND));
        userDomain.validateActive(requester);

        final ListRecipientsResponse response = new ListRecipientsResponse();
        final Set<Role> roles = kudoDomain.allowedRecipientRoles(requester.getRole());
        if (roles.isEmpty()) {
            return response;
        }

        final String text = request.getText() == null ? "" : request.getText().trim();
        if (!text.isEmpty() && text.length() < MIN_TEXT_LENGTH) {
            throw new DomainException(MessageCodes.RECIPIENT_TEXT_TOO_SHORT, String.valueOf(MIN_TEXT_LENGTH));
        }

        final UUID authorId = requester.getId();
        final Map<UUID, User> found = new LinkedHashMap<>();
        if (text.isEmpty()) {
            add(found, directory.findRecent(authorId, roles, recentLimit));
        }
        add(found, directory.findInSharedPrograms(authorId, roles, text, limit));
        add(found, directory.findLeadership(authorId, roles, text, limit));
        if (!text.isEmpty()) {
            add(found, directory.search(authorId, roles, text, limit));
        }

        found.values().stream().limit(limit).map(ListRecipientsUseCase::toResponse).forEach(response.getItems()::add);
        return response;
    }

    private static void add(final Map<UUID, User> found, final Iterable<User> candidates) {
        candidates.forEach(user -> found.putIfAbsent(user.getId(), user));
    }

    private static RecipientResponse toResponse(final User user) {
        final RecipientResponse item = new RecipientResponse();
        item.setId(user.getId());
        item.setName(user.getName());
        item.setEmail(user.getEmail());
        item.setRole(user.getRole());
        return item;
    }
}
