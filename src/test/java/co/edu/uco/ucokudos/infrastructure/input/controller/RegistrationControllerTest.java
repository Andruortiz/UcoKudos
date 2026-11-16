package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

    private static final String UNREGISTERED = "UNREGISTERED";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Value("${kudos.registration.allowed-email-domains}") List<String> domains;

    private static String email(final String domain) {
        return "p" + UUID.randomUUID().toString().substring(0, 8) + "@" + domain;
    }

    private static RequestPostProcessor token(final String oid, final String email) {
        return jwt().jwt(j -> j.claim("oid", oid).claim("name", "Pedro Pérez").claim("email", email))
                .authorities(new SimpleGrantedAuthority(UNREGISTERED));
    }

    @Test
    void theRegistrationAcceptsBothInstitutionalDomainsAndTheDemoDirectory() {
        assertEquals(List.of("uco.net.co", "uco.edu.co", "ucokudosgmail.onmicrosoft.com"), domains);
    }

    @Test
    void anUnregisteredSessionCreatesAnActiveAccountFromTheTokenAlone() throws Exception {
        final String oid = UUID.randomUUID().toString();
        final String email = email("ucokudosgmail.onmicrosoft.com");

        mvc.perform(post("/api/v1/registrations").with(token(oid, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        final User user = users.findByDirectoryId(oid).orElseThrow();
        assertEquals(email, user.getEmail());
        assertEquals("Pedro Pérez", user.getName());
        assertEquals(Role.STUDENT, user.getRole());
        assertNull(user.getPhone());
        assertTrue(user.isActive() && user.isEmailVerified());
    }

    @Test
    void withoutASessionTheRegistrationIs401() throws Exception {
        mvc.perform(post("/api/v1/registrations")).andExpect(status().isUnauthorized());
    }

    @Test
    void aTokenOfAnAlreadyRegisteredUserCannotRegisterAgain() throws Exception {
        mvc.perform(post("/api/v1/registrations")
                        .with(jwt().jwt(j -> j.claim("oid", "x").claim("email", "x@uco.net.co"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anEmailOutsideTheAllowedDomainsIs422() throws Exception {
        mvc.perform(post("/api/v1/registrations").with(token(UUID.randomUUID().toString(), "pedro@gmail.com")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL));
    }

    @Test
    void theSameEmailTwiceIs409() throws Exception {
        final String email = email("uco.edu.co");
        mvc.perform(post("/api/v1/registrations").with(token(UUID.randomUUID().toString(), email)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/registrations").with(token(UUID.randomUUID().toString(), email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageCodes.REGISTRATION_ALREADY_EXISTS));
    }

    @Test
    void anUnregisteredSessionCannotUseTheRestOfTheApi() throws Exception {
        mvc.perform(get("/api/v1/me").with(token("oid-x", "x@uco.net.co")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageCodes.AUTH_USER_NOT_REGISTERED));
    }

    @Test
    void theRestOfTheApiStillNeedsASession() throws Exception {
        mvc.perform(post("/api/v1/kudos")).andExpect(status().isUnauthorized());
    }
}
