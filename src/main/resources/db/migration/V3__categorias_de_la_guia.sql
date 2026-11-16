INSERT INTO category (id, name, active, created_at) VALUES
    (gen_random_uuid(), 'Rendimiento Académico', true, now()),
    (gen_random_uuid(), 'Mejor Investigador', true, now()),
    (gen_random_uuid(), 'Empatía', true, now()),
    (gen_random_uuid(), 'Comunicación Asertiva', true, now()),
    (gen_random_uuid(), 'Orientación al Logro', true, now()),
    (gen_random_uuid(), 'Trabajo en Equipo', true, now()),
    (gen_random_uuid(), 'Trabajo Bajo Presión', true, now()),
    (gen_random_uuid(), 'Visión Estratégica', true, now()),
    (gen_random_uuid(), 'Innovación', true, now()),
    (gen_random_uuid(), 'Liderazgo', true, now()),
    (gen_random_uuid(), 'Transformación Digital', true, now()),
    (gen_random_uuid(), 'Mentor Inspirador', true, now()),
    (gen_random_uuid(), 'Espíritu UCO', true, now()),
    (gen_random_uuid(), 'Servicio a la Comunidad', true, now()),
    (gen_random_uuid(), 'Compromiso Institucional', true, now())
ON CONFLICT (name) DO UPDATE SET active = true;

UPDATE category SET active = false
WHERE name IN ('Colaboración', 'Excelencia académica', 'Compromiso');
