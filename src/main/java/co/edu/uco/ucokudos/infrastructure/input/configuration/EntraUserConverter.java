package co.edu.uco.ucokudos.infrastructure.input.configuration;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EntraUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository users;
    private final UserDomain userDomain;
    private final List<String> allowedDomains;

    public EntraUserConverter(final UserRepository users, final UserDomain userDomain,
                              @Value("${kudos.allowed-email-domains}") final List<String> allowedDomains) {
        this.users = users;
        this.userDomain = userDomain;
        this.allowedDomains = allowedDomains;
    }

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {
        final String objectId = jwt.getClaimAsString("oid");
        if (objectId == null) {
            throw rejected(MessageCodes.AUTH_TOKEN_INVALID);
        }
        final String email = emailOf(jwt);
        if (email == null) {
            throw rejected(MessageCodes.AUTH_EMAIL_NOT_ALLOWED);
        }
        try {
            userDomain.validateEmail(email, allowedDomains);
        } catch (final DomainException e) {
            throw rejected(MessageCodes.AUTH_EMAIL_NOT_ALLOWED);
        }
        final User user = users.findByDirectoryId(objectId)
                .or(() -> linkByEmail(email, objectId))
                .orElseThrow(() -> rejected(MessageCodes.AUTH_USER_NOT_REGISTERED));
        return new JwtAuthenticationToken(jwt, AuthorityUtils.NO_AUTHORITIES, user.getId().toString());
    }

    /** Members of the directory often have no "email" claim; their user name is the address. Guest #EXT# names are not. */
    private static String emailOf(final Jwt jwt) {
        final String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        final String name = jwt.getClaimAsString("preferred_username");
        return name != null && name.contains("@") && !name.contains("#EXT#") ? name : null;
    }

    private Optional<User> linkByEmail(final String email, final String objectId) {
        return users.findByEmail(email).map(user -> {
            user.setDirectoryId(objectId);
            return users.save(user);
        });
    }

    private static OAuth2AuthenticationException rejected(final String code) {
        return new OAuth2AuthenticationException(new OAuth2Error(code));
    }
}
