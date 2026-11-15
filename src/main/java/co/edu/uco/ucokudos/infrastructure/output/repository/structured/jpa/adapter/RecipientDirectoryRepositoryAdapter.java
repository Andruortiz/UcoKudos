package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.UserJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.KudoJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.RecipientDirectoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RecipientDirectoryRepositoryAdapter implements RecipientDirectoryRepository {

    private static final List<Role> GLOBAL_ROLES = List.of(Role.RECTOR, Role.ACADEMIC_COORDINATOR);

    private final UserJpaRepository userRepository;
    private final KudoJpaRepository kudoRepository;
    private final UserEntityMapper mapper;

    public RecipientDirectoryRepositoryAdapter(
            final UserJpaRepository userRepository,
            final KudoJpaRepository kudoRepository,
            final UserEntityMapper mapper) {
        this.userRepository = userRepository;
        this.kudoRepository = kudoRepository;
        this.mapper = mapper;
    }

    @Override
    public List<User> findRecent(final UUID authorId, final Collection<Role> roles, final int limit) {
        final List<UUID> ids = kudoRepository.findRecentRecipientIds(authorId, roles, UserStatus.ACTIVE,
                PageRequest.of(0, limit));
        if (ids.isEmpty()) {
            return List.of();
        }
        final Map<UUID, UserJpaEntity> byId = userRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(UserJpaEntity::getId, Function.identity()));
        return ids.stream().map(byId::get).map(mapper::toDomain).toList();
    }

    @Override
    public List<User> findInSharedPrograms(final UUID authorId, final Collection<Role> roles, final String text,
                                           final int limit) {
        return userRepository.findInSharedPrograms(authorId, roles, UserStatus.ACTIVE, text, PageRequest.of(0, limit))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<User> findLeadership(final UUID authorId, final Collection<Role> roles, final String text,
                                     final int limit) {
        return userRepository.findLeadership(authorId, roles, GLOBAL_ROLES, UserStatus.ACTIVE, text,
                        PageRequest.of(0, limit))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<User> search(final UUID authorId, final Collection<Role> roles, final String text, final int limit) {
        return userRepository.search(authorId, roles, UserStatus.ACTIVE, text, PageRequest.of(0, limit))
                .stream().map(mapper::toDomain).toList();
    }
}
