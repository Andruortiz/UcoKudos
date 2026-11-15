INSERT INTO category (id, name, active, created_at) VALUES
    (gen_random_uuid(), 'Colaboración', true, now()),
    (gen_random_uuid(), 'Empatía', true, now()),
    (gen_random_uuid(), 'Liderazgo', true, now()),
    (gen_random_uuid(), 'Excelencia académica', true, now()),
    (gen_random_uuid(), 'Compromiso', true, now())
ON CONFLICT (name) DO NOTHING;
