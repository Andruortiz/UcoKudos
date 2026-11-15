package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.request.ListKudosRequest;
import co.edu.uco.ucokudos.service.input.dto.response.KudoResponse;
import co.edu.uco.ucokudos.service.input.dto.response.ListKudosResponse;
import co.edu.uco.ucokudos.service.input.port.ListKudosPort;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListKudosUseCase implements ListKudosPort {

    private final KudoRepository kudoRepository;
    private final UserRepository users;
    private final CategoryRepository categories;

    public ListKudosUseCase(final KudoRepository kudoRepository, final UserRepository users,
                            final CategoryRepository categories) {
        this.kudoRepository = kudoRepository;
        this.users = users;
        this.categories = categories;
    }

    @Override
    public ListKudosResponse execute(final ListKudosRequest request) {
        final List<Kudo> kudos = kudoRepository.findAllByFilters(
                request.getUserId(), request.getCategoryId(), request.getDirection());

        final Set<UUID> userIds = new HashSet<>();
        final Set<UUID> categoryIds = new HashSet<>();
        for (final Kudo kudo : kudos) {
            userIds.add(kudo.getAuthorId());
            userIds.add(kudo.getRecipientId());
            categoryIds.add(kudo.getCategoryId());
        }
        // two queries for the whole page, not one per kudo
        final Map<UUID, User> userById = users.findByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        final Map<UUID, Category> categoryById = categories.findByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        final ListKudosResponse response = new ListKudosResponse();
        response.setItems(kudos.stream().map(kudo -> {
            final KudoResponse item = new KudoResponse();
            item.setId(kudo.getId());
            item.setAuthorId(kudo.getAuthorId());
            item.setAuthorName(nameOf(userById.get(kudo.getAuthorId())));
            item.setRecipientId(kudo.getRecipientId());
            item.setRecipientName(nameOf(userById.get(kudo.getRecipientId())));
            item.setCategoryId(kudo.getCategoryId());
            final Category category = categoryById.get(kudo.getCategoryId());
            item.setCategoryName(category == null ? null : category.getName());
            item.setContent(kudo.getContent());
            item.setCreatedAt(kudo.getCreatedAt());
            return item;
        }).toList());
        return response;
    }

    private static String nameOf(final User user) {
        return user == null ? null : user.getName();
    }
}
