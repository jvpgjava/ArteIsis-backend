-- INSERT "comum" sem pgcrypto: hashes BCrypt gerados com Spring BCryptPasswordEncoder (strength 10).
-- Palavras-passe: AdminDemo123!  e  ClienteDemo123!
-- Se o e-mail já existir, não faz nada (ON CONFLICT).

INSERT INTO app_user (id, email, password_hash, role, full_name, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin_demo@arteisis.local',
    '$2a$10$EKfHT6faTsg05cHnsnXYy.nOCQZ3Qpjn5Fq73UUegXyODv5HEaaFC',
    'ADMIN',
    'Administrador demo',
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (id, email, password_hash, role, full_name, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'cliente_demo@arteisis.local',
    '$2a$10$6RHmC.xxz5zdeyxWiB/ge.CLE4RRp0i3Rlgq7qzwqg79iLGbfPaL6',
    'CUSTOMER',
    'Cliente demo',
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;
