INSERT INTO users (id, directory_id, name, email, phone, role, status, email_verified, phone_verified, created_at) VALUES
    ('00000000-0000-0000-0000-000000000001', 'dev-ana', 'Ana Estudiante', 'ana@uco.net.co', NULL, 'STUDENT', 'ACTIVE', true, true, now()),
    ('00000000-0000-0000-0000-000000000002', 'dev-luis', 'Luis Docente', 'luis@uco.net.co', NULL, 'TEACHER', 'ACTIVE', true, true, now()),
    ('00000000-0000-0000-0000-000000000003', 'dev-carlos', 'Carlos Estudiante', 'carlos@uco.net.co', NULL, 'STUDENT', 'ACTIVE', true, true, now())
ON CONFLICT DO NOTHING;
