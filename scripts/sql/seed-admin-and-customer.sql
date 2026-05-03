CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO app_user (id, email, password_hash, role, full_name, created_at, updated_at)
SELECT gen_random_uuid(),
       'admin_demo@arteisis.local',
       crypt($$AdminDemo123!$$::text, gen_salt('bf'::text)),
       'ADMIN',
       'Administrador demo',
       now(),
       now()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE lower(email) = lower('admin_demo@arteisis.local'));

INSERT INTO app_user (id, email, password_hash, role, full_name, created_at, updated_at)
SELECT gen_random_uuid(),
       'cliente_demo@arteisis.local',
       crypt($$ClienteDemo123!$$::text, gen_salt('bf'::text)),
       'CUSTOMER',
       'Cliente demo',
       now(),
       now()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE lower(email) = lower('cliente_demo@arteisis.local'));
