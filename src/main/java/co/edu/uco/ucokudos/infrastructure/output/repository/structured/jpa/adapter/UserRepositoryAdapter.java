package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;

    public UserRepositoryAdapter(
            final UserJpaRepository jpaRepository,
            final UserEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(final UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public User save(final User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(user)));
    }

    @Override
    public List<User> findByIds(final Collection<UUID> ids) {
        return ids.isEmpty() ? List.of() : jpaRepository.findByIdIn(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<User> findByDirectoryId(final String directoryId) {
        return jpaRepository.findByDirectoryId(directoryId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return jpaRepository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }
}
