package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.UserJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    String TEXT_FILTER = """
            (:text = '' or lower(u.name) like lower(concat('%', :text, '%'))
                        or lower(u.email) like lower(concat('%', :text, '%')))
            """;

    String MY_PROGRAMS = "select a.programId from UserProgramJpaEntity a where a.userId = :authorId";

    @Query("select u from UserJpaEntity u where u.id in :ids")
    List<UserJpaEntity> findByIdIn(@Param("ids") Collection<UUID> ids);

    @Query("""
            select distinct u from UserJpaEntity u, UserProgramJpaEntity up
            where up.userId = u.id
              and up.programId in (""" + MY_PROGRAMS + """
            )
              and u.status = :status and u.role in :roles and u.id <> :authorId
              and """ + TEXT_FILTER + """
            order by u.name
            """)
    List<UserJpaEntity> findInSharedPrograms(@Param("authorId") UUID authorId, @Param("roles") Collection<Role> roles,
                                             @Param("status") UserStatus status, @Param("text") String text,
                                             Pageable pageable);

    @Query("""
            select u from UserJpaEntity u
            where u.status = :status and u.role in :roles and u.id <> :authorId
              and (u.id in (select p.coordinatorId from ProgramJpaEntity p where p.id in (""" + MY_PROGRAMS + """
            ))
                or u.id in (select f.deanId from FacultyJpaEntity f where f.id in
                    (select p.facultyId from ProgramJpaEntity p where p.id in (""" + MY_PROGRAMS + """
            )))
                or u.role in :globalRoles)
              and """ + TEXT_FILTER + """
            order by u.name
            """)
    List<UserJpaEntity> findLeadership(@Param("authorId") UUID authorId, @Param("roles") Collection<Role> roles,
                                       @Param("globalRoles") Collection<Role> globalRoles,
                                       @Param("status") UserStatus status, @Param("text") String text,
                                       Pageable pageable);

    @Query("""
            select u from UserJpaEntity u
            where u.status = :status and u.role in :roles and u.id <> :authorId
              and """ + TEXT_FILTER + """
            order by u.name
            """)
    List<UserJpaEntity> search(@Param("authorId") UUID authorId, @Param("roles") Collection<Role> roles,
                               @Param("status") UserStatus status, @Param("text") String text, Pageable pageable);
}
