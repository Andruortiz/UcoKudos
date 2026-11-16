package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired MockMvc mvc;
    @Autowired UserJpaRepository userJpa;
    @Autowired UserEntityMapper userMapper;

    private User saveUser(final UserStatus status) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setDirectoryId("dir-" + user.getId());
        user.setName("Ana Estudiante");
        user.setEmail(user.getId() + "@uco.net.co");
        user.setRole(Role.STUDENT);
        user.setStatus(status);
        user.setEmailVerified(true);
        user.setPhoneVerified(false);
        user.setCreatedAt(Instant.now());
        userJpa.save(userMapper.toJpa(user));
        return user;
    }

    @Test
    void meReturnsMyOwnProfileWithoutAPhoneYet() throws Exception {
        final User ana = saveUser(UserStatus.ACTIVE);

        mvc.perform(get("/api/v1/me").with(user(ana.getId().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ana.getId().toString()))
                .andExpect(jsonPath("$.name").value("Ana Estudiante"))
                .andExpect(jsonPath("$.email").value(ana.getEmail()))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.phone").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.phoneVerified").value(false));
    }

    @Test
    void aNewPhoneIsSavedUnverifiedAndStaysSaved() throws Exception {
        final User ana = saveUser(UserStatus.ACTIVE);

        mvc.perform(put("/api/v1/me/phone").with(user(ana.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"phone\":\" +573001234567 \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+573001234567"))
                .andExpect(jsonPath("$.phoneVerified").value(false));

        mvc.perform(get("/api/v1/me").with(user(ana.getId().toString())))
                .andExpect(jsonPath("$.phone").value("+573001234567"));
    }

    @Test
    void aPhoneWithoutTheInternationalFormatIs422WithTheCatalogMessage() throws Exception {
        final User ana = saveUser(UserStatus.ACTIVE);

        mvc.perform(put("/api/v1/me/phone").with(user(ana.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"phone\":\"3001234567\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_PHONE_INVALID))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("formato internacional")));
    }

    @Test
    void aBodyWithoutPhoneIs422() throws Exception {
        final User ana = saveUser(UserStatus.ACTIVE);

        mvc.perform(put("/api/v1/me/phone").with(user(ana.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_PHONE_INVALID));
    }

    @Test
    void anInactiveUserCannotChangeThePhone() throws Exception {
        final User inactive = saveUser(UserStatus.INACTIVE);

        mvc.perform(put("/api/v1/me/phone").with(user(inactive.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"phone\":\"+573001234567\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_NOT_ACTIVE));
    }

    @Test
    void aSessionThatIsNotAUserIdIsUserNotFound() throws Exception {
        mvc.perform(get("/api/v1/me").with(user("no-es-un-uuid")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_NOT_FOUND));
    }

    @Test
    void aUserThatDoesNotExistInTheDatabaseIsUserNotFound() throws Exception {
        mvc.perform(get("/api/v1/me").with(user(UUID.randomUUID().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_NOT_FOUND));
    }

    @Test
    void withoutASessionBothEndpointsAreRefused() throws Exception {
        mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/me/phone").contentType(MediaType.APPLICATION_JSON).content("{\"phone\":\"+573001234567\"}"))
                .andExpect(status().isUnauthorized());
    }
}
