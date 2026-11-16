package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import co.edu.uco.ucokudos.service.output.port.notification.SmsSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PhoneVerificationFlowTest.FakeSms.class)
class PhoneVerificationFlowTest {

    static final List<String> CODES = new ArrayList<>();

    @TestConfiguration
    static class FakeSms {
        @Bean
        @Primary
        SmsSender fakeSmsSender() {
            return (phone, code) -> CODES.add(code);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired UserJpaRepository userJpa;
    @Autowired UserEntityMapper userMapper;

    private User saveUser(final String phone) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setDirectoryId("dir-" + user.getId());
        user.setName("Ana Estudiante");
        user.setEmail(user.getId() + "@uco.net.co");
        user.setPhone(phone);
        user.setRole(Role.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setCreatedAt(Instant.now());
        userJpa.save(userMapper.toJpa(user));
        return user;
    }

    @Test
    void theWholeFlowFromAskingForTheCodeToAVerifiedPhone() throws Exception {
        final User ana = saveUser("+573001234567");

        mvc.perform(post("/api/v1/me/phone/verification").with(user(ana.getId().toString())))
                .andExpect(status().isNoContent());
        final String code = CODES.get(CODES.size() - 1);

        mvc.perform(post("/api/v1/me/phone/verification/confirm").with(user(ana.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneVerified").value(true));
        mvc.perform(get("/api/v1/me").with(user(ana.getId().toString())))
                .andExpect(jsonPath("$.phoneVerified").value(true));
    }

    @Test
    void aWrongCodeIs422AndAskingAgainRightAwayIs429() throws Exception {
        final User ana = saveUser("+573001234567");
        mvc.perform(post("/api/v1/me/phone/verification").with(user(ana.getId().toString())))
                .andExpect(status().isNoContent());
        final String wrong = CODES.get(CODES.size() - 1).equals("000000") ? "111111" : "000000";

        mvc.perform(post("/api/v1/me/phone/verification/confirm").with(user(ana.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + wrong + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.PHONE_CODE_INVALID));
        mvc.perform(post("/api/v1/me/phone/verification").with(user(ana.getId().toString())))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(MessageCodes.PHONE_CODE_TOO_SOON));
    }

    @Test
    void withoutAPhoneThereIsNothingToVerify() throws Exception {
        final User ana = saveUser(null);

        mvc.perform(post("/api/v1/me/phone/verification").with(user(ana.getId().toString())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(MessageCodes.USER_PHONE_INVALID));
    }

    @Test
    void changingThePhoneAfterVerifyingMakesItUnverifiedAgain() throws Exception {
        final User ana = saveUser("+573001234567");
        mvc.perform(post("/api/v1/me/phone/verification").with(user(ana.getId().toString())));
        mvc.perform(post("/api/v1/me/phone/verification/confirm").with(user(ana.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + CODES.get(CODES.size() - 1) + "\"}"));

        mvc.perform(put("/api/v1/me/phone").with(user(ana.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"phone\":\"+573009998877\"}"))
                .andExpect(jsonPath("$.phoneVerified").value(false));
    }

    @Test
    void bothEndpointsNeedASession() throws Exception {
        mvc.perform(post("/api/v1/me/phone/verification")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/me/phone/verification/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"123456\"}")).andExpect(status().isUnauthorized());
    }
}
