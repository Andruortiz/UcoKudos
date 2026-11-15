INSERT INTO users (id, directory_id, name, email, phone, role, status, email_verified, phone_verified, created_at) VALUES
    ('00000000-0000-0000-0000-000000000001', 'dev-ana', 'Ana Estudiante', 'ana@ucokudosgmail.onmicrosoft.com', NULL, 'STUDENT', 'ACTIVE', true, true, now()),
    ('00000000-0000-0000-0000-000000000002', 'dev-luis', 'Luis Docente', 'luis@ucokudosgmail.onmicrosoft.com', NULL, 'TEACHER', 'ACTIVE', true, true, now()),
    ('00000000-0000-0000-0000-000000000003', 'dev-carlos', 'Carlos Estudiante', 'carlos@ucokudosgmail.onmicrosoft.com', NULL, 'STUDENT', 'ACTIVE', true, true, now())
-- the email follows the Entra user; directory_id is left alone because the first login links the real object id
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name;
