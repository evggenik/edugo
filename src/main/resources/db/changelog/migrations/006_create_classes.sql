CREATE TABLE classes(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    teacher_id BIGINT,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    CONSTRAINT uq_classes_name_academic_year UNIQUE (name, academic_year)
);