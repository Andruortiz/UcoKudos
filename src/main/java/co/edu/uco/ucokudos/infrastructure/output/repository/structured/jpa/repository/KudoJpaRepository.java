package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.KudoJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface KudoJpaRepository extends JpaRepository<KudoJpaEntity, UUID> {

    long countByAuthorIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID authorId, Instant from, Instant to);

    @Query("""
            select k.recipientId from KudoJpaEntity k, UserJpaEntity u
            where u.id = k.recipientId and k.authorId = :authorId
              and u.status = :status and u.role in :roles and u.id <> :authorId
            group by k.recipientId
            order by max(k.createdAt) desc
            """)
    List<UUID> findRecentRecipientIds(@Param("authorId") UUID authorId, @Param("roles") Collection<Role> roles,
                                      @Param("status") UserStatus status, Pageable pageable);
}
