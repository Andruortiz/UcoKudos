package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;

import java.util.List;

public class KudoDomain {

    public static final int MAX_ATTACHMENTS = 5;
    public static final long MAX_TOTAL_BYTES = 10L * 1024 * 1024;

    public void validateContent(final String content) {
        if (content == null || content.isBlank()) {
            throw new DomainException(MessageCodes.KUDO_CONTENT_REQUIRED);
        }
    }

    public void validateNotSelf(final User author, final User recipient) {
        if (author.getId().equals(recipient.getId())) {
            throw new DomainException(MessageCodes.KUDO_RECIPIENT_SELF);
        }
    }

    public void validateRoleCanSend(final Role from, final Role to) {
        final int fromRank = rank(from);
        final int toRank = rank(to);
        final boolean allowed = fromRank > 0 && toRank > 0 && switch (fromRank) {
            case 1 -> toRank <= 2;
            case 2 -> toRank >= 2;
            default -> true;
        };
        if (!allowed) {
            throw new DomainException(MessageCodes.KUDO_ROLE_NOT_ALLOWED);
        }
    }

    public void validateCategoryActive(final Category category) {
        if (!category.isActive()) {
            throw new DomainException(MessageCodes.KUDO_CATEGORY_NOT_ACTIVE);
        }
    }

    public void validateDailyLimit(final long sentToday, final int limit) {
        if (sentToday >= limit) {
            throw new DomainException(MessageCodes.KUDO_DAILY_LIMIT, String.valueOf(limit));
        }
    }

    public void validateAttachments(final List<Attachment> attachments) {
        if (attachments.size() > MAX_ATTACHMENTS) {
            throw new DomainException(MessageCodes.KUDO_ATTACHMENT_TOO_MANY, String.valueOf(MAX_ATTACHMENTS));
        }
        long total = 0;
        for (final Attachment attachment : attachments) {
            final String type = attachment.contentType();
            if (type == null || !(type.startsWith("image/") || type.equals("application/pdf"))) {
                throw new DomainException(MessageCodes.KUDO_ATTACHMENT_TYPE);
            }
            total += attachment.size();
        }
        if (total > MAX_TOTAL_BYTES) {
            throw new DomainException(MessageCodes.KUDO_ATTACHMENT_TOO_LARGE, String.valueOf(MAX_TOTAL_BYTES / 1024 / 1024));
        }
    }

    private static int rank(final Role role) {
        return switch (role) {
            case ADMINISTRATOR -> 0;
            case STUDENT -> 1;
            case TEACHER -> 2;
            case PROGRAM_COORDINATOR -> 3;
            case FACULTY_COORDINATOR -> 4;
            case ACADEMIC_COORDINATOR -> 5;
            case RECTOR -> 6;
        };
    }
}
