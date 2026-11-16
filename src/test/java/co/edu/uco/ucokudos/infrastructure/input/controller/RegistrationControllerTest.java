package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired MockMvc mvc;
    @Value("${kudos.registration.allowed-email-domains}") java.util.List<String> domains;
    @Value("${kudos.login.allowed-email-domains}") java.util.List<String> loginDomains;

    private String body(final String email, final String role) {
        return "{\"name\":\"Pedro Pérez\",\"email\":\"%s\",\"phone\":\"+573001234567\",\"role\":%s}"
                .formatted(email, role);
    }

    private String email() {
        return "p" + UUID.randomUUID().toString().substring(0, 8) + "@" + domains.get(0);
    }

    @Test
    void anAnonymousPersonCanSendARequestAndItStaysPending() throws Exception {
        mvc.perform(post("/api/v1/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(body(email(), "\"STUDENT\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void theLoginAcceptsTheInstitutionAndTheDemoDirectoryWhileTheRegistrationOnlyTheInstitution() {
        org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of("uco.net.co", "uco.edu.co"), domains);
        org.junit.jupiter.api.Assertions.assertTrue(loginDomains.containsAll(
                java.util.List.of("uco.net.co", "uco.edu.co", "ucokudosgmail.onmicrosoft.com")), loginDomains.toString());
    }

    @Test
    void bothInstitutionalDomainsAreAcceptedForRegistration() throws Exception {
        for (final String domain : new String[]{"uco.net.co", "uco.edu.co"}) {
            mvc.perform(post("/api/v1/registrations").contentType(MediaType.APPLICATION_JSON)
                            .content(body("p" + UUID.randomUUID().toString().substring(0, 8) + "@" + domain,
                                    "\"TEACHER\"")))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void theDemoDirectoryDomainIsNotAnInstitutionalEmailForRegistration() throws Exception {
        mvc.perform(post("/api/v1/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("ana@ucokudosgmail.onmicrosoft.com", "\"STUDENT\"")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL));
    }

    @Test
    void anEmailOutsideTheInstitutionIs422() throws Exception {
        mvc.perform(post("/api/v1/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(body("pedro@gmail.com", "\"STUDENT\"")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL));
    }

    @Test
    void aRoleThatCannotSelfRegisterIs422() throws Exception {
        mvc.perform(post("/api/v1/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(body(email(), "\"RECTOR\"")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.REGISTRATION_ROLE_INVALID));
    }

    @Test
    void theSameEmailTwiceIs409() throws Exception {
        final String email = email();
        mvc.perform(post("/api/v1/registrations").contentType(MediaType.APPLICATION_JSON)
                .content(body(email, "\"TEACHER\""))).andExpect(status().isCreated());

        mvc.perform(post("/api/v1/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(body(email, "\"TEACHER\"")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(MessageCodes.REGISTRATION_ALREADY_EXISTS));
    }

    @Test
    void theRestOfTheApiStillNeedsASession() throws Exception {
        mvc.perform(post("/api/v1/kudos")).andExpect(status().isUnauthorized());
    }
}
