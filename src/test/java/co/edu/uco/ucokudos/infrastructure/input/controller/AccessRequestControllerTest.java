package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccessRequestControllerTest {

    @Autowired MockMvc mvc;

    private static MockHttpServletRequestBuilder request(final String email, final String client) {
        return post("/api/v1/access-requests").header("X-Forwarded-For", client)
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\"}");
    }

    private static String client() {
        return "10.0.0." + UUID.randomUUID().getLeastSignificantBits() % 200 + "-" + UUID.randomUUID();
    }

    @Test
    void itIsPublicAndTheDomainIsCheckedBeforeAnythingElse() throws Exception {
        mvc.perform(request("alguien@gmail.com", client()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL));
    }

    @Test
    void withoutDirectoryCredentialsAnAllowedEmailGets503() throws Exception {
        mvc.perform(request("nuevo@uco.net.co", client()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(MessageCodes.ACCESS_UNAVAILABLE));
    }

    @Test
    void theSameClientIsLimitedAfterFiveRequests() throws Exception {
        final String client = client();
        for (int i = 0; i < 5; i++) {
            mvc.perform(request("alguien@gmail.com", client)).andExpect(status().isUnprocessableEntity());
        }

        mvc.perform(request("alguien@gmail.com", client))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(MessageCodes.ACCESS_TOO_MANY));
        mvc.perform(request("alguien@gmail.com", client()))
                .andExpect(status().isUnprocessableEntity());
    }
}
