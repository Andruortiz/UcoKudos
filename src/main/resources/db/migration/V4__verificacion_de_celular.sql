CREATE TABLE phone_verification (
    user_id uuid NOT NULL,
    code_hash character varying(64) NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    last_sent_at timestamp(6) with time zone NOT NULL,
    window_start timestamp(6) with time zone NOT NULL,
    attempts integer NOT NULL,
    sent_count integer NOT NULL,
    CONSTRAINT phone_verification_pkey PRIMARY KEY (user_id)
);

ALTER TABLE audit_record DROP CONSTRAINT audit_record_action_check;

ALTER TABLE audit_record ADD CONSTRAINT audit_record_action_check CHECK (action IN (
    'KUDO_PUBLISHED', 'NOTIFICATION_SENT', 'NOTIFICATION_FAILED', 'PHONE_UPDATED',
    'PHONE_VERIFICATION_SENT', 'PHONE_VERIFIED'));
