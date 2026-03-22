CREATE TABLE grades(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    lesson_id BIGINT,
    period_id BIGINT,
    value SMALLINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    comment VARCHAR(500),
    graded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    FOREIGN KEY (period_id) REFERENCES periods(id),
    CONSTRAINT chk_grades_lesson_id_period_id CHECK (lesson_id IS NOT NULL OR period_id IS NOT NULL),
    CONSTRAINT chk_grades_value CHECK (value >= 2 AND value <= 5)
);

CREATE INDEX idx_grades_student_id ON grades(student_id);
CREATE INDEX idx_grades_period_id ON grades(period_id);