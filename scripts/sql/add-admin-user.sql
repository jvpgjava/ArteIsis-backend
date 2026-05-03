-- Requer extensão pgcrypto (PostgreSQL). Executado por scripts/add-admin-user.ps1 com -v email, pwd, fname.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO app_user (id, email, password_hash, role, full_name, created_at, updated_at)
SELECT gen_random_uuid(),
       :'email',
       crypt(:'pwd', gen_salt('bf')),
       'ADMIN',
       :'fname',
       now(),
       now()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE lower(email) = lower(:'email'));
