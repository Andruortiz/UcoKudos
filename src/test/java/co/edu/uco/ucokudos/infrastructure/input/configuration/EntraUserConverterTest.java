package co.edu.uco.ucokudos.infrastructure.input.configuration;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntraUserConverterTest {

    private final User ana = new User();
    private final Map<UUID, User> saved = new HashMap<>();
    private final EntraUserConverter converter;

    EntraUserConverterTest() {
        ana.setId(UuidHelper.generate());
        ana.setEmail("ana@uco.net.co");
        ana.setDirectoryId("dev-ana");
        final UserRepository users = new UserRepository() {
            public Optional<User> findById(final UUID id) { return Optional.empty(); }
            public User save(final User user) { saved.put(user.getId(), user); return user; }
            public List<User> findByIds(final Collection<UUID> ids) { return List.of(); }
            public Optional<User> findByDirectoryId(final String id) {
                return id.equals(ana.getDirectoryId()) ? Optional.of(ana) : Optional.empty();
            }
            public Optional<User> findByEmail(final String email) {
                return email.equalsIgnoreCase(ana.getEmail()) ? Optional.of(ana) : Optional.empty();
            }
        };
        converter = new EntraUserConverter(users, new UserDomain(), List.of("uco.net.co"));
    }

    private static Jwt jwt(final Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), claims);
    }

    private String rejectionOf(final Map<String, Object> claims) {
        return assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(jwt(claims)))
                .getError().getErrorCode();
    }

    @Test
    void aKnownObjectIdBecomesTheUserUuid() {
        final Authentication auth = converter.convert(jwt(Map.of("oid", "dev-ana", "email", "ana@uco.net.co")));

        assertEquals(ana.getId().toString(), auth.getName());
    }

    @Test
    void theFirstLoginLinksTheObjectIdByEmail() {
        final Authentication auth = converter.convert(jwt(Map.of("oid", "new-oid", "email", "ANA@uco.net.co")));

        assertEquals(ana.getId().toString(), auth.getName());
        assertEquals("new-oid", saved.get(ana.getId()).getDirectoryId());
    }

    @Test
    void aMemberWithoutEmailClaimUsesTheirUserName() {
        final Authentication auth = converter.convert(
                jwt(Map.of("oid", "new-oid", "preferred_username", "ana@uco.net.co")));

        assertEquals(ana.getId().toString(), auth.getName());
    }

    @Test
    void anEmailOutsideTheInstitutionIsRejected() {
        assertEquals(MessageCodes.AUTH_EMAIL_NOT_ALLOWED,
                rejectionOf(Map.of("oid", "dev-ana", "email", "ana@gmail.com")));
    }

    @Test
    void aGuestUserNameIsNotTakenAsEmail() {
        assertEquals(MessageCodes.AUTH_EMAIL_NOT_ALLOWED,
                rejectionOf(Map.of("oid", "x", "preferred_username", "ana_uco.net.co#EXT#@tenant.onmicrosoft.com")));
    }

    @Test
    void anInstitutionalUserNotInTheDatabaseIsNotRegistered() {
        assertEquals(MessageCodes.AUTH_USER_NOT_REGISTERED,
                rejectionOf(Map.of("oid", "x", "email", "otro@uco.net.co")));
    }

    @Test
    void aTokenWithoutObjectIdIsInvalid() {
        assertEquals(MessageCodes.AUTH_TOKEN_INVALID, rejectionOf(Map.of("email", "ana@uco.net.co")));
    }
}
