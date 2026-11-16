package co.edu.uco.ucokudos.bootstrap;

import co.edu.uco.ucokudos.domain.PhoneVerificationDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.service.input.port.ConfirmPhoneVerificationPort;
import co.edu.uco.ucokudos.service.input.port.RequestPhoneVerificationPort;
import co.edu.uco.ucokudos.service.output.port.notification.SmsSender;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.PhoneVerificationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import co.edu.uco.ucokudos.service.usecase.ConfirmPhoneVerificationUseCase;
import co.edu.uco.ucokudos.service.usecase.RequestPhoneVerificationUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class PhoneVerificationConfiguration {

    @Bean
    PhoneVerificationDomain phoneVerificationDomain() {
        return new PhoneVerificationDomain();
    }

    @Bean
    RequestPhoneVerificationPort requestPhoneVerificationPort(
            final UserRepository users, final PhoneVerificationRepository verifications,
            final AuditRepository audits, final UserDomain userDomain,
            final PhoneVerificationDomain verificationDomain, final SmsSender sms, final Clock clock,
            @Value("${kudos.phone-verification.code-ttl}") final Duration codeTtl,
            @Value("${kudos.phone-verification.resend-cooldown}") final Duration cooldown,
            @Value("${kudos.phone-verification.max-sends-per-hour}") final int maxSendsPerHour) {
        return new RequestPhoneVerificationUseCase(users, verifications, audits, userDomain, verificationDomain,
                sms, clock, codeTtl, cooldown, maxSendsPerHour);
    }

    @Bean
    ConfirmPhoneVerificationPort confirmPhoneVerificationPort(
            final UserRepository users, final PhoneVerificationRepository verifications,
            final AuditRepository audits, final UserDomain userDomain,
            final PhoneVerificationDomain verificationDomain, final Clock clock,
            @Value("${kudos.phone-verification.max-attempts}") final int maxAttempts) {
        return new ConfirmPhoneVerificationUseCase(users, verifications, audits, userDomain, verificationDomain,
                clock, maxAttempts);
    }
}
