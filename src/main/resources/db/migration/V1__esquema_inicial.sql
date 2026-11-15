-- Esquema inicial de UCO Kudos. Generado a partir de las entidades JPA (ADR-0005).
-- No se edita: los cambios nuevos van en un V2, V3... nuevo.


CREATE TABLE audit_record (
    occurred_at timestamp(6) with time zone NOT NULL,
    actor_id uuid,
    id uuid NOT NULL,
    resource_id uuid NOT NULL,
    action character varying(40) NOT NULL,
    resource_type character varying(40) NOT NULL,
    correlation_id character varying(100) NOT NULL,
    CONSTRAINT audit_record_action_check CHECK (((action)::text = ANY ((ARRAY['KUDO_PUBLISHED'::character varying, 'NOTIFICATION_SENT'::character varying, 'NOTIFICATION_FAILED'::character varying, 'PHONE_UPDATED'::character varying])::text[])))
);

CREATE TABLE category (
    active boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    name character varying(100) NOT NULL
);

CREATE TABLE faculty (
    dean_id uuid,
    id uuid NOT NULL,
    name character varying(200) NOT NULL
);

CREATE TABLE idempotency_key (
    created_at timestamp(6) with time zone NOT NULL,
    author_id uuid NOT NULL,
    id uuid NOT NULL,
    kudo_id uuid NOT NULL,
    request_hash character varying(64) NOT NULL,
    idempotency_key character varying(100) NOT NULL
);

CREATE TABLE kudo (
    created_at timestamp(6) with time zone NOT NULL,
    author_id uuid NOT NULL,
    category_id uuid NOT NULL,
    id uuid NOT NULL,
    recipient_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    content text NOT NULL,
    CONSTRAINT kudo_status_check CHECK (((status)::text = ANY ((ARRAY['PUBLISHED'::character varying, 'VOTOED'::character varying])::text[])))
);

CREATE TABLE kudo_attachment (
    id uuid NOT NULL,
    kudo_id uuid NOT NULL,
    content_type character varying(100) NOT NULL,
    content bytea NOT NULL,
    name character varying(255) NOT NULL
);

CREATE TABLE notification (
    attempts integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    next_attempt_at timestamp(6) with time zone,
    sent_at timestamp(6) with time zone,
    id uuid NOT NULL,
    kudo_id uuid NOT NULL,
    channel character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    last_error character varying(1000),
    CONSTRAINT notification_channel_check CHECK (((channel)::text = ANY ((ARRAY['EMAIL'::character varying, 'WHATSAPP'::character varying, 'PUSH'::character varying])::text[]))),
    CONSTRAINT notification_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying, 'SKIPPED'::character varying])::text[])))
);

CREATE TABLE program (
    coordinator_id uuid,
    faculty_id uuid NOT NULL,
    id uuid NOT NULL,
    name character varying(200) NOT NULL
);

CREATE TABLE registration_request (
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    phone character varying(20) NOT NULL,
    role character varying(30) NOT NULL,
    status character varying(30) NOT NULL,
    name character varying(200) NOT NULL,
    email character varying(254) NOT NULL,
    CONSTRAINT registration_request_role_check CHECK (((role)::text = ANY ((ARRAY['ADMINISTRATOR'::character varying, 'RECTOR'::character varying, 'ACADEMIC_COORDINATOR'::character varying, 'FACULTY_COORDINATOR'::character varying, 'PROGRAM_COORDINATOR'::character varying, 'TEACHER'::character varying, 'STUDENT'::character varying])::text[]))),
    CONSTRAINT registration_request_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);

CREATE TABLE user_program (
    id uuid NOT NULL,
    program_id uuid NOT NULL,
    user_id uuid NOT NULL
);

CREATE TABLE users (
    email_verified boolean NOT NULL,
    phone_verified boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    phone character varying(20),
    role character varying(30) NOT NULL,
    status character varying(30) NOT NULL,
    directory_id character varying(64) NOT NULL,
    name character varying(200) NOT NULL,
    email character varying(254) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMINISTRATOR'::character varying, 'RECTOR'::character varying, 'ACADEMIC_COORDINATOR'::character varying, 'FACULTY_COORDINATOR'::character varying, 'PROGRAM_COORDINATOR'::character varying, 'TEACHER'::character varying, 'STUDENT'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING_VERIFICATION'::character varying, 'ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);

ALTER TABLE ONLY audit_record
    ADD CONSTRAINT audit_record_pkey PRIMARY KEY (id);

ALTER TABLE ONLY category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);

ALTER TABLE ONLY faculty
    ADD CONSTRAINT faculty_pkey PRIMARY KEY (id);

ALTER TABLE ONLY idempotency_key
    ADD CONSTRAINT idempotency_key_pkey PRIMARY KEY (id);

ALTER TABLE ONLY kudo_attachment
    ADD CONSTRAINT kudo_attachment_pkey PRIMARY KEY (id);

ALTER TABLE ONLY kudo
    ADD CONSTRAINT kudo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);

ALTER TABLE ONLY program
    ADD CONSTRAINT program_pkey PRIMARY KEY (id);

ALTER TABLE ONLY registration_request
    ADD CONSTRAINT registration_request_pkey PRIMARY KEY (id);

ALTER TABLE ONLY category
    ADD CONSTRAINT uk_category_name UNIQUE (name);

ALTER TABLE ONLY faculty
    ADD CONSTRAINT uk_faculty_name UNIQUE (name);

ALTER TABLE ONLY idempotency_key
    ADD CONSTRAINT uk_idempotency_key_author UNIQUE (idempotency_key, author_id);

ALTER TABLE ONLY notification
    ADD CONSTRAINT uk_notification_kudo_channel UNIQUE (kudo_id, channel);

ALTER TABLE ONLY program
    ADD CONSTRAINT uk_program_faculty_name UNIQUE (faculty_id, name);

ALTER TABLE ONLY user_program
    ADD CONSTRAINT uk_user_program_user_program UNIQUE (user_id, program_id);

ALTER TABLE ONLY users
    ADD CONSTRAINT uk_users_directory_id UNIQUE (directory_id);

ALTER TABLE ONLY users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE ONLY user_program
    ADD CONSTRAINT user_program_pkey PRIMARY KEY (id);

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

CREATE INDEX idx_audit_resource ON audit_record USING btree (resource_type, resource_id);

CREATE INDEX idx_kudo_attachment_kudo ON kudo_attachment USING btree (kudo_id);

CREATE INDEX idx_kudo_author_created ON kudo USING btree (author_id, created_at);

CREATE INDEX idx_kudo_recipient_created ON kudo USING btree (recipient_id, created_at);

CREATE INDEX idx_notification_status_next ON notification USING btree (status, next_attempt_at);

CREATE INDEX idx_registration_email_status ON registration_request USING btree (email, status);

CREATE INDEX idx_user_program_program ON user_program USING btree (program_id);

