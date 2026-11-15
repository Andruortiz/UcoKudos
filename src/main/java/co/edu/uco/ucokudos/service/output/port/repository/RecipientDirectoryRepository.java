package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RecipientDirectoryRepository {

    List<User> findRecent(UUID authorId, Collection<Role> roles, int limit);

    List<User> findInSharedPrograms(UUID authorId, Collection<Role> roles, String text, int limit);

    List<User> findLeadership(UUID authorId, Collection<Role> roles, String text, int limit);

    List<User> search(UUID authorId, Collection<Role> roles, String text, int limit);
}
