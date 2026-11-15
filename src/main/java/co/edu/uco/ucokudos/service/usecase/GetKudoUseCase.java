package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.response.AttachmentResponse;
import co.edu.uco.ucokudos.service.input.dto.response.KudoDetailResponse;
import co.edu.uco.ucokudos.service.input.port.GetKudoPort;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GetKudoUseCase implements GetKudoPort {

    private final KudoRepository kudos;
    private final UserRepository users;
    private final CategoryRepository categories;

    public GetKudoUseCase(final KudoRepository kudos, final UserRepository users, final CategoryRepository categories) {
        this.kudos = kudos;
        this.users = users;
        this.categories = categories;
    }

    @Override
    public KudoDetailResponse execute(final UUID requesterId, final UUID kudoId) {
        final Kudo kudo = kudos.findById(kudoId).orElseThrow(() -> new DomainException(MessageCodes.KUDO_NOT_FOUND));
        // Only the author and the recipient may read it; a stranger gets the same answer as for a missing Kudo,
        // so the id does not reveal that the Kudo exists.
        if (!requesterId.equals(kudo.getAuthorId()) && !requesterId.equals(kudo.getRecipientId())) {
            throw new DomainException(MessageCodes.KUDO_NOT_FOUND);
        }

        final Map<UUID, User> userById = users.findByIds(Set.of(kudo.getAuthorId(), kudo.getRecipientId())).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        final Category category = categories.findByIds(Set.of(kudo.getCategoryId())).stream().findFirst().orElse(null);

        final KudoDetailResponse response = new KudoDetailResponse();
        response.setId(kudo.getId());
        response.setAuthorId(kudo.getAuthorId());
        response.setAuthorName(nameOf(userById.get(kudo.getAuthorId())));
        response.setRecipientId(kudo.getRecipientId());
        response.setRecipientName(nameOf(userById.get(kudo.getRecipientId())));
        response.setCategoryId(kudo.getCategoryId());
        response.setCategoryName(category == null ? null : category.getName());
        response.setContent(kudo.getContent());
        response.setCreatedAt(kudo.getCreatedAt());
        final List<AttachmentResponse> attachments = kudo.getAttachments().stream()
                .map(a -> new AttachmentResponse(a.name(), a.contentType(), a.size()))
                .toList();
        response.setAttachments(attachments);
        return response;
    }

    private static String nameOf(final User user) {
        return user == null ? null : user.getName();
    }
}
