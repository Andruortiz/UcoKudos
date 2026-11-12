package co.edu.uco.ucokudos.crosscutting.message;

public final class MessageCodes {

    public static final String USER_NOT_ACTIVE = "user.not.active";
    public static final String USER_NOT_VERIFIED = "user.not.verified";
    public static final String USER_NAME_REQUIRED = "user.name.required";
    public static final String USER_EMAIL_INVALID = "user.email.invalid";
    public static final String USER_EMAIL_NOT_INSTITUTIONAL = "user.email.not.institutional";
    public static final String USER_PHONE_INVALID = "user.phone.invalid";
    public static final String USER_NOT_FOUND = "user.not.found";
    public static final String CATEGORY_NOT_FOUND = "category.not.found";
    public static final String KUDO_CONTENT_REQUIRED = "kudo.content.required";
    public static final String KUDO_RECIPIENT_SELF = "kudo.recipient.self";
    public static final String KUDO_ROLE_NOT_ALLOWED = "kudo.role.not.allowed";
    public static final String KUDO_CATEGORY_NOT_ACTIVE = "kudo.category.not.active";
    public static final String KUDO_DAILY_LIMIT = "kudo.daily.limit";
    public static final String KUDO_ATTACHMENT_TOO_MANY = "kudo.attachment.too.many";
    public static final String KUDO_ATTACHMENT_TOO_LARGE = "kudo.attachment.too.large";
    public static final String KUDO_ATTACHMENT_TYPE = "kudo.attachment.type";
    public static final String KUDO_ATTACHMENT_INFECTED = "kudo.attachment.infected";
    public static final String KUDO_SCANNER_UNAVAILABLE = "kudo.scanner.unavailable";
    public static final String KUDO_IDEMPOTENCY_KEY_REQUIRED = "kudo.idempotency.key.required";
    public static final String KUDO_IDEMPOTENCY_CONFLICT = "kudo.idempotency.conflict";

    private MessageCodes() {
    }
}
