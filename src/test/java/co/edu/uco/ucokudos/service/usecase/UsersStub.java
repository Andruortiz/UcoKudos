package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/** Test double: finds users with the given function and remembers what was saved. */
class UsersStub implements UserRepository {

    final Map<UUID, User> saved = new HashMap<>();
    private final Function<UUID, Optional<User>> finder;

    UsersStub(final Function<UUID, Optional<User>> finder) {
        this.finder = finder;
    }

    @Override
    public Optional<User> findById(final UUID id) {
        return finder.apply(id);
    }

    @Override
    public java.util.List<User> findByIds(final java.util.Collection<UUID> ids) {
        return ids.stream().map(finder).flatMap(Optional::stream).toList();
    }

    @Override
    public Optional<User> findByDirectoryId(final String directoryId) {
        return saved.values().stream().filter(u -> directoryId.equals(u.getDirectoryId())).findFirst();
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        return saved.values().stream().filter(u -> email.equalsIgnoreCase(u.getEmail())).findFirst();
    }

    @Override
    public User save(final User user) {
        saved.put(user.getId(), user);
        return user;
    }
}
