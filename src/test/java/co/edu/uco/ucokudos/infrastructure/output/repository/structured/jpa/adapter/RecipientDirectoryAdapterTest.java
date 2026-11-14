package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.domain.model.KudoStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.FacultyJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.KudoJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.ProgramJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.UserProgramJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.FacultyJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.KudoJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.ProgramJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserProgramJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RecipientDirectoryAdapterTest {

    private static final Instant NOW = Instant.parse("2026-11-12T15:00:00Z");
    private static final Set<Role> STUDENT_TARGETS = EnumSet.of(Role.STUDENT, Role.TEACHER);
    private static final Set<Role> TEACHER_TARGETS = EnumSet.of(Role.TEACHER, Role.PROGRAM_COORDINATOR,
            Role.FACULTY_COORDINATOR, Role.ACADEMIC_COORDINATOR, Role.RECTOR);

    @Autowired RecipientDirectoryRepositoryAdapter directory;
    @Autowired UserJpaRepository userJpa;
    @Autowired UserEntityMapper userMapper;
    @Autowired FacultyJpaRepository facultyJpa;
    @Autowired ProgramJpaRepository programJpa;
    @Autowired UserProgramJpaRepository userProgramJpa;
    @Autowired KudoJpaRepository kudoJpa;

    private String tag;
    private User author;
    private User sameProgramStudent;
    private User sameProgramTeacher;
    private User otherProgramStudent;
    private User inactiveStudent;
    private User programCoordinator;
    private User otherCoordinator;
    private User dean;
    private User otherDean;
    private User rector;
    private User academicCoordinator;
    private UUID programOne;
    private UUID programTwo;

    private User saveUser(final Role role, final String name, final UserStatus status) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setDirectoryId("dir-" + user.getId());
        user.setName(name + " " + tag);
        user.setEmail(user.getId() + "@uco.net.co");
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(NOW);
        userJpa.save(userMapper.toJpa(user));
        return user;
    }

    private void enroll(final User user, final UUID program) {
        final UserProgramJpaEntity link = new UserProgramJpaEntity();
        link.setId(UuidHelper.generate());
        link.setUserId(user.getId());
        link.setProgramId(program);
        userProgramJpa.save(link);
    }

    private UUID saveProgram(final UUID facultyId, final User coordinator) {
        final ProgramJpaEntity program = new ProgramJpaEntity();
        program.setId(UuidHelper.generate());
        program.setName("Programa " + program.getId());
        program.setFacultyId(facultyId);
        program.setCoordinatorId(coordinator.getId());
        programJpa.save(program);
        return program.getId();
    }

    private UUID saveFaculty(final User dean) {
        final FacultyJpaEntity faculty = new FacultyJpaEntity();
        faculty.setId(UuidHelper.generate());
        faculty.setName("Facultad " + faculty.getId());
        faculty.setDeanId(dean.getId());
        facultyJpa.save(faculty);
        return faculty.getId();
    }

    private void saveKudo(final User from, final User to, final Instant at) {
        final KudoJpaEntity kudo = new KudoJpaEntity();
        kudo.setId(UuidHelper.generate());
        kudo.setAuthorId(from.getId());
        kudo.setRecipientId(to.getId());
        kudo.setCategoryId(UuidHelper.generate());
        kudo.setContent("Gracias");
        kudo.setCreatedAt(at);
        kudo.setStatus(KudoStatus.PUBLISHED);
        kudoJpa.save(kudo);
    }

    private List<UUID> ids(final List<User> users) {
        return users.stream().map(User::getId).toList();
    }

    @BeforeEach
    void setUp() {
        tag = UUID.randomUUID().toString().substring(0, 8);
        author = saveUser(Role.STUDENT, "Autor", UserStatus.ACTIVE);
        sameProgramStudent = saveUser(Role.STUDENT, "Ana", UserStatus.ACTIVE);
        sameProgramTeacher = saveUser(Role.TEACHER, "Luis", UserStatus.ACTIVE);
        otherProgramStudent = saveUser(Role.STUDENT, "Carlos", UserStatus.ACTIVE);
        inactiveStudent = saveUser(Role.STUDENT, "Inactivo", UserStatus.INACTIVE);
        programCoordinator = saveUser(Role.PROGRAM_COORDINATOR, "CoordUno", UserStatus.ACTIVE);
        otherCoordinator = saveUser(Role.PROGRAM_COORDINATOR, "CoordDos", UserStatus.ACTIVE);
        dean = saveUser(Role.FACULTY_COORDINATOR, "DecanoUno", UserStatus.ACTIVE);
        otherDean = saveUser(Role.FACULTY_COORDINATOR, "DecanoDos", UserStatus.ACTIVE);
        rector = saveUser(Role.RECTOR, "Rector", UserStatus.ACTIVE);
        academicCoordinator = saveUser(Role.ACADEMIC_COORDINATOR, "Academico", UserStatus.ACTIVE);

        programOne = saveProgram(saveFaculty(dean), programCoordinator);
        programTwo = saveProgram(saveFaculty(otherDean), otherCoordinator);
        enroll(author, programOne);
        enroll(sameProgramStudent, programOne);
        enroll(sameProgramTeacher, programOne);
        enroll(inactiveStudent, programOne);
        enroll(otherProgramStudent, programTwo);
    }

    @Test
    void sharedProgramsReturnsOnlyActivePeopleOfMyProgramsWithAllowedRoles() {
        final List<UUID> found = ids(directory.findInSharedPrograms(author.getId(), STUDENT_TARGETS, "", 50));

        assertTrue(found.contains(sameProgramStudent.getId()));
        assertTrue(found.contains(sameProgramTeacher.getId()));
        assertFalse(found.contains(author.getId()));
        assertFalse(found.contains(otherProgramStudent.getId()));
        assertFalse(found.contains(inactiveStudent.getId()));
    }

    @Test
    void sharedProgramsAppliesTheTextFilterByNameOrEmail() {
        assertEquals(List.of(sameProgramStudent.getId()),
                ids(directory.findInSharedPrograms(author.getId(), STUDENT_TARGETS, "ana " + tag, 50)));
        assertEquals(List.of(sameProgramTeacher.getId()),
                ids(directory.findInSharedPrograms(author.getId(), STUDENT_TARGETS,
                        sameProgramTeacher.getEmail(), 50)));
    }

    @Test
    void leadershipReturnsMyCoordinatorMyDeanAndTheGlobalCargosButNotOtherStructures() {
        final List<UUID> teacherView = ids(directory.findLeadership(sameProgramTeacher.getId(), TEACHER_TARGETS, "", 50));
        assertTrue(teacherView.contains(programCoordinator.getId()));
        assertFalse(teacherView.contains(sameProgramStudent.getId()));

        final List<UUID> mine = ids(directory.findLeadership(author.getId(), EnumSet.allOf(Role.class), "", 50));
        assertTrue(mine.contains(programCoordinator.getId()));
        assertTrue(mine.contains(dean.getId()));
        assertTrue(mine.contains(rector.getId()));
        assertTrue(mine.contains(academicCoordinator.getId()));
        assertFalse(mine.contains(otherCoordinator.getId()));
        assertFalse(mine.contains(otherDean.getId()));
    }

    @Test
    void searchFindsPeopleOutsideMyProgramsByNameOrEmailIgnoringCase() {
        final List<UUID> byName = ids(directory.search(author.getId(), STUDENT_TARGETS, "CARLOS " + tag.toUpperCase(), 50));
        assertEquals(List.of(otherProgramStudent.getId()), byName);
        final List<UUID> byEmail = ids(directory.search(author.getId(), STUDENT_TARGETS,
                otherProgramStudent.getEmail(), 50));
        assertEquals(List.of(otherProgramStudent.getId()), byEmail);
        assertFalse(ids(directory.search(author.getId(), STUDENT_TARGETS, "Inactivo " + tag, 50))
                .contains(inactiveStudent.getId()));
    }

    @Test
    void recentReturnsTheLastDistinctActiveRecipientsMostRecentFirst() {
        saveKudo(author, sameProgramStudent, NOW.minusSeconds(300));
        saveKudo(author, otherProgramStudent, NOW.minusSeconds(100));
        saveKudo(author, sameProgramStudent, NOW.minusSeconds(10));
        saveKudo(author, inactiveStudent, NOW);

        final List<UUID> found = ids(directory.findRecent(author.getId(), STUDENT_TARGETS, 10));

        assertEquals(List.of(sameProgramStudent.getId(), otherProgramStudent.getId()), found);
        assertEquals(1, directory.findRecent(author.getId(), STUDENT_TARGETS, 1).size());
    }

    @Test
    void theLimitIsApplied() {
        assertEquals(1, directory.findInSharedPrograms(author.getId(), STUDENT_TARGETS, "", 1).size());
    }
}
