package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.UserProgramJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserProgramJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RecipientControllerTest {

    @Autowired MockMvc mvc;
    @Autowired UserJpaRepository userJpa;
    @Autowired UserEntityMapper userMapper;
    @Autowired UserProgramJpaRepository userProgramJpa;

    private User author;
    private User classmate;

    private User saveUser(final Role role, final String name) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setDirectoryId("dir-" + user.getId());
        user.setName(name);
        user.setEmail(user.getId() + "@uco.net.co");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
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

    @BeforeEach
    void setUp() {
        final UUID program = UuidHelper.generate();
        author = saveUser(Role.STUDENT, "Autor");
        classmate = saveUser(Role.STUDENT, "Compañera");
        enroll(author, program);
        enroll(classmate, program);
    }

    @Test
    void listsMyClassmatesWithoutTextAndHidesThePhone() throws Exception {
        mvc.perform(get("/api/v1/recipients").with(user(author.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')].name", classmate.getId()).value("Compañera"))
                .andExpect(jsonPath("$.items[?(@.id == '%s')].role", classmate.getId()).value("STUDENT"))
                .andExpect(jsonPath("$.items[0].phone").doesNotExist());
    }

    @Test
    void searchFindsAnyoneByTextOfThreeOrMoreLetters() throws Exception {
        final User far = saveUser(Role.STUDENT, "Zoraida Lejana");
        mvc.perform(get("/api/v1/recipients").param("q", "zoraida").with(user(author.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')].email", far.getId()).value(far.getEmail()));
    }

    @Test
    void textShorterThanThreeLettersIs400() throws Exception {
        mvc.perform(get("/api/v1/recipients").param("q", "zo").with(user(author.getId().toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageCodes.RECIPIENT_TEXT_TOO_SHORT))
                .andExpect(jsonPath("$.message").value("Escribe al menos 3 letras para buscar"));
    }

    @Test
    void withoutAuthenticationIs401() throws Exception {
        mvc.perform(get("/api/v1/recipients")).andExpect(status().isUnauthorized());
    }
}
