CREATE TABLE parent_students(
    parent_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (parent_id, student_id),
    FOREIGN KEY (parent_id) REFERENCES users(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT chk_parent_not_student CHECK (parent_id != student_id)
);

CREATE INDEX idx_parent_students_student_id ON parent_students(student_id);