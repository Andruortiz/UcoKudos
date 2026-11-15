package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Role;
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


    public void validateCanList(final Role requester, final Role target) {
        final boolean allowed = switch (requester) {
            case ADMINISTRATOR -> target == Role.ADMINISTRATOR || target == Role.RECTOR;
            case RECTOR -> target == Role.ACADEMIC_COORDINATOR;
            case ACADEMIC_COORDINATOR -> target == Role.FACULTY_COORDINATOR;
            case FACULTY_COORDINATOR -> target == Role.PROGRAM_COORDINATOR;
            case PROGRAM_COORDINATOR -> target == Role.STUDENT || target == Role.TEACHER;
            default -> false;
        };
        if (!allowed) {
            throw new DomainException(MessageCodes.USER_LIST_NOT_ALLOWED);
        }
    }


}
