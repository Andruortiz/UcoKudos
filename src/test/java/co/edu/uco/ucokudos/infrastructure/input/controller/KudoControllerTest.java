package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.CategoryEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.CategoryJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class KudoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired UserJpaRepository userJpa;
    @Autowired UserEntityMapper userMapper;
    @Autowired CategoryJpaRepository categoryJpa;
    @Autowired CategoryEntityMapper categoryMapper;

    private UUID authorId;
    private UUID recipientId;
    private UUID categoryId;

    private UUID saveUser(final Role role) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setDirectoryId("dir-" + user.getId());
        user.setName("Usuario");
        user.setEmail(user.getId() + "@uco.net.co");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        userJpa.save(userMapper.toJpa(user));
        return user.getId();
    }

    @BeforeEach
    void setUp() {
        authorId = saveUser(Role.STUDENT);
        recipientId = saveUser(Role.TEACHER);
        final Category category = new Category();
        category.setId(UuidHelper.generate());
        category.setName("Categoria " + category.getId());
        category.setCreatedAt(Instant.now());
        categoryJpa.save(categoryMapper.toJpa(category));
        categoryId = category.getId();
    }

    private MockMultipartHttpServletRequestBuilder post(final UUID recipient, final String content, final String key) {
        final String json = "{\"recipientId\":\"%s\",\"categoryId\":\"%s\",\"content\":\"%s\"}"
                .formatted(recipient, categoryId, content);
        final MockMultipartHttpServletRequestBuilder request = multipart("/api/v1/kudos")
                .file(new MockMultipartFile("kudo", "", "application/json", json.getBytes()));
        return key == null ? request : (MockMultipartHttpServletRequestBuilder) request.header("Idempotency-Key", key);
    }

    @Test
    void publishReturns201WithThreePendingNotificationsAndReplayReturnsTheSameKudo() throws Exception {
        final String key = "key-" + authorId;
        final String id = com.jayway.jsonpath.JsonPath.read(
                mvc.perform(post(recipientId, "Gracias", key).with(user(authorId.toString())).with(csrf()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value("PUBLISHED"))
                        .andExpect(jsonPath("$.notifications.length()").value(3))
                        .andExpect(jsonPath("$.notifications[0].status").value("PENDING"))
                        .andReturn().getResponse().getContentAsString(), "$.id");

        mvc.perform(post(recipientId, "Gracias", key).with(user(authorId.toString())).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void sameKeyWithOtherContentIs409() throws Exception {
        final String key = "key-" + authorId;
        mvc.perform(post(recipientId, "Uno", key).with(user(authorId.toString())).with(csrf()))
                .andExpect(status().isCreated());
        mvc.perform(post(recipientId, "Dos", key).with(user(authorId.toString())).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageCodes.KUDO_IDEMPOTENCY_CONFLICT));
    }

    @Test
    void sendingToYourselfIs422WithTheCatalogText() throws Exception {
        mvc.perform(post(authorId, "Hola", "k-" + authorId).with(user(authorId.toString())).with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.KUDO_RECIPIENT_SELF))
                .andExpect(jsonPath("$.message").value("No puedes enviarte un Kudo a ti mismo"));
    }

    @Test
    void missingKeyIs400() throws Exception {
        mvc.perform(post(recipientId, "Hola", null).with(user(authorId.toString())).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MessageCodes.KUDO_IDEMPOTENCY_KEY_REQUIRED));
    }

    @Test
    void blankContentIs422() throws Exception {
        mvc.perform(post(recipientId, " ", "k-" + authorId).with(user(authorId.toString())).with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.REQUEST_INVALID));
    }

    @Test
    void anAttachmentIs503UntilTheScannerExists() throws Exception {
        mvc.perform(post(recipientId, "Con adjunto", "k-" + authorId)
                        .file(new MockMultipartFile("files", "a.pdf", "application/pdf", new byte[]{1}))
                        .with(user(authorId.toString())).with(csrf()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(MessageCodes.KUDO_SCANNER_UNAVAILABLE));
    }

    @Test
    void withoutAuthenticationIs401() throws Exception {
        mvc.perform(post(recipientId, "Hola", "k-1").with(csrf())).andExpect(status().isUnauthorized());
    }
}
