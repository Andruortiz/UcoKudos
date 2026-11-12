package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.User;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

public class UserDomain {

    private static final Pattern PHONE = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public void validateActive(final User user) {
        if (!user.isActive()) {
            throw new DomainException(MessageCodes.USER_NOT_ACTIVE);
        }
    }

    // RN-09: an account is only activated with a verified email and a verified phone
    public void validateCanActivate(final User user) {
        if (!user.isEmailVerified() || !user.isPhoneVerified()) {
            throw new DomainException(MessageCodes.USER_NOT_VERIFIED);
        }
    }

    public void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException(MessageCodes.USER_NAME_REQUIRED);
        }
    }

    public void validateEmail(final String email, final Collection<String> allowedDomains) {
        final int at = email == null ? -1 : email.lastIndexOf('@');
        if (at <= 0 || at == email.length() - 1) {
            throw new DomainException(MessageCodes.USER_EMAIL_INVALID);
        }
        final String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (allowedDomains.stream().noneMatch(allowed -> allowed.equalsIgnoreCase(domain))) {
            throw new DomainException(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL, String.join(", ", allowedDomains));
        }
    }

    public void validatePhone(final String phone) {
        if (phone == null || !PHONE.matcher(phone).matches()) {
            throw new DomainException(MessageCodes.USER_PHONE_INVALID);
        }
    }
}
