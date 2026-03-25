ALTER TABLE users
    ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users
SET onboarding_completed = TRUE
WHERE career_track IS NOT NULL;
