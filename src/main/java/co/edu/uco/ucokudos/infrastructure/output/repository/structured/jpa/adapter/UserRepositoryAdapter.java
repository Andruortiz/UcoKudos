package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
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
}
