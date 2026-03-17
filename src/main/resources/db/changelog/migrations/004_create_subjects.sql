CREATE TABLE subjects(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE UNIQUE INDEX ux_subjects_name_lower
    ON subjects (LOWER(name));