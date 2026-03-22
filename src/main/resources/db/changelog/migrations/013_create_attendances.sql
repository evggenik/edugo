CREATE TABLE attendances(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    CONSTRAINT uq_attendances_student_id_lesson_id UNIQUE (student_id, lesson_id)
);

CREATE INDEX idx_attendances_lesson_id ON attendances(lesson_id);