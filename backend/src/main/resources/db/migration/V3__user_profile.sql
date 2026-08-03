ALTER TABLE users ADD COLUMN display_name VARCHAR(80);
UPDATE users SET display_name = split_part(email, '@', 1) WHERE display_name IS NULL;
ALTER TABLE users ALTER COLUMN display_name SET NOT NULL;

ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
ALTER TABLE users ADD COLUMN location VARCHAR(120);
ALTER TABLE users ADD COLUMN rating_average NUMERIC(3, 2);
ALTER TABLE users ADD COLUMN rating_count INTEGER NOT NULL DEFAULT 0;
