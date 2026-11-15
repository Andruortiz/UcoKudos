package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    User save(User user);

    Optional<User> findByDirectoryId(String directoryId);

    Optional<User> findByEmail(String email);

    List<User> findByIds(Collection<UUID> ids);
}
